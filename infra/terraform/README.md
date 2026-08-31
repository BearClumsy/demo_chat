# infra/terraform

Infrastructure-as-code for the AWS staging/prod environments described in
`docs/wiki/Plan/infrastructure.md` and `docs/wiki/Plan/local-vs-aws.md`.

## Status: lint-only

There is no AWS account for this project yet, so this code is **written and CI-linted but never
applied**. `terraform plan` / `apply` have not been run. Treat every module as a reviewable draft:
resource shapes and wiring are meant to be correct, but AWS-specific values (AMI ids, ACM cert
ARNs, Secrets Manager ARNs) are left as `variable`s with `TODO` markers, and a few details can only
be verified once `plan` runs against a real account.

This corresponds to **Phase 3b, item 1** of `docs/wiki/Plan/roadmap.md`, minus its AWS-touching
tail. Still deferred (need an account): `terraform apply`, ECR + `deploy-staging` workflow, the
knowledge-base reindex job, live-Bedrock validation, load testing.

## Layout

```
infra/terraform/
├── .tflint.hcl              tflint config (terraform + aws rulesets)
├── modules/
│   ├── vpc/                 VPC, 2–3 AZ × {public, app, data} subnets, IGW, per-AZ NAT, routes
│   ├── alb/                 application LB, :80→:443 redirect, HTTPS listener, ip target group
│   ├── ecs-service/         ECS cluster + Fargate service + task def + task SG + log group
│   ├── rds-postgres/        RDS Postgres 16, subnet group, SG (5432 from app CIDRs)
│   ├── keyspaces/           Amazon Keyspaces keyspace + one modelled table (see caveat below)
│   ├── qdrant-ec2/          single EC2 Qdrant host + data EBS volume + SG (6333/6334 from app CIDRs)
│   ├── msk/                 MSK cluster + SG (9092/9094 from app CIDRs)
│   └── bedrock-iam/         ECS task role (bedrock:InvokeModel) + execution role (+ secrets read)
└── envs/
    ├── staging/             2 AZ, single-AZ RDS, 2 tasks — main.tf / variables.tf / outputs.tf / *.tfvars
    └── prod/                3 AZ, multi-AZ RDS, 3 tasks — duplicated from staging on purpose
```

`envs/staging` and `envs/prod` are **deliberately duplicated** rather than sharing a root module —
same reasoning as the duplicated `application-{staging,prod}.properties` (see
`docs/wiki/Plan/local-vs-aws.md`).

## The env-var contract

Each env's `main.tf` builds a `plaintext_env` map and its `outputs.tf` exposes it as
`container_env`. Every key there must line up with a property in
`modules/server/src/main/resources/application-{staging,prod}.properties`:

| Container env var            | Source                                    |
|------------------------------|-------------------------------------------|
| `POSTGRES_HOST` / `_PORT` / `_DB` / `_USER` | `module.rds_postgres`             |
| `CASSANDRA_CONTACT_POINTS` / `_PORT` / `_LOCAL_DATACENTER` / `_KEYSPACE` | Keyspaces endpoint + `module.keyspaces` |
| `QDRANT_HOST`                | `module.qdrant`                           |
| `QDRANT_PORT` / `_USE_TLS` / `_INITIALIZE_SCHEMA` | constants (6334 / false / false)  |
| `KAFKA_BOOTSTRAP_SERVERS`    | `module.msk`                              |
| `POSTGRES_PASSWORD`, `CASSANDRA_PASSWORD`, `QDRANT_API_KEY` | Secrets Manager via `task_secret_arns` — **not** in `container_env` |

`POSTGRES_SCHEMA` is intentionally **not** a variable: the app's Flyway `V1` migration hardcodes
schema `demo_chat`, so it is fixed, not configurable.

## Known gaps / TODO

- **State backend** — `backend "s3"` blocks in `envs/*/versions.tf` are commented out until the
  state bucket + DynamoDB lock table exist. CI runs `terraform init -backend=false`.
- **Keyspaces tables** — only `open_chats_by_bucket` is modelled. `chat_history` and
  `dialogue_state` use the `ChatMessage` UDT and a wider column set; their source of truth is the
  JPA entities and `modules/server/src/main/resources/db/cassandra/*.cql`. Port them (plus an
  `aws_keyspaces_type`) before an apply.
- **Data-tier ingress by CIDR, not SG** — `rds-postgres`, `qdrant-ec2` and `msk` allow the app
  subnet CIDRs rather than referencing the ECS task security group, to avoid a module dependency
  cycle with the container-env wiring. Tighten to SG references once the env root owns a shared
  app SG.
- **RDS password** — plain `variable` for now; switch to `manage_master_user_password` +
  Secrets Manager.
- **Qdrant AMI / user_data** — `qdrant_ami_id` has no default; the host needs an image that runs
  the `qdrant/qdrant` container (bake it or add `user_data`).
- **No `s3-cloudfront` module** — the React client's static hosting is out of scope here.
- **`msk` module** is beyond `infrastructure.md`'s original list but the `KAFKA_BOOTSTRAP_SERVERS`
  contract requires it.

## Linting locally

Needs `terraform` (or `tofu`) and `tflint` on PATH. From the repo root:

```
make tf-lint
```

which runs, against `infra/terraform`:

```
terraform fmt -check -recursive
terraform -chdir=envs/staging init -backend=false && terraform -chdir=envs/staging validate
terraform -chdir=envs/prod    init -backend=false && terraform -chdir=envs/prod    validate
tflint --recursive
```

The `terraform-lint` GitHub Actions workflow runs the same steps on every PR that touches
`infra/**`. No AWS credentials are configured in that job.
