# infra/terraform

Infrastructure-as-code for the AWS staging/prod environments described in
`docs/wiki/Plan/infrastructure.md`, `docs/wiki/Plan/local-vs-aws.md` and
`docs/wiki/Plan/kubernetes.md`.

## Status: lint-only

There is no AWS account for this project yet, so this code is **written and CI-linted but never
applied**. `terraform plan` / `apply` have not been run. Treat every module as a reviewable draft:
resource shapes and wiring are meant to be correct, but AWS-specific values (AMI ids, ACM cert
ARNs, the GitHub org, the admin CIDR) are left as `variable`s with `TODO` markers, and a few
details can only be verified once `plan` runs against a real account.

> **Do not `apply` before the S3 state backend + DynamoDB lock table exist.** The Kubernetes
> modules (`k8s-cluster`, `alb-k8s`, `ecr`, `github-oidc`) create real stateful infrastructure
> (Auto Scaling groups, an internal NLB, a KMS key, an S3 bucket, IAM roles). Losing local state
> after a partial apply would orphan all of it. `envs/*/versions.tf` still carries the
> `backend "s3"` block commented out for lint parity only.

## Deploy path: Kubernetes on EC2

The app runs on a **self-managed kubeadm cluster on EC2**, fronted by the **NGINX Ingress
Controller**. The Kubernetes manifests live in `infra/k8s/`; this directory provisions the cluster
and the surrounding AWS resources.

```
Internet ─▶ ALB (alb-k8s, instance target, TLS/ACM, 300s idle+drain for SSE)
         ─▶ worker ASG :30080  ─▶ ingress-nginx (DaemonSet)  ─▶ Service ─▶ Deployment
```

Backing services are unchanged and external: `rds-postgres`, `keyspaces`, `qdrant-ec2`, `msk`.

### Modules

```
infra/terraform/
├── .tflint.hcl              tflint config (terraform + aws rulesets)
├── modules/
│   ├── vpc/                 VPC, 2–3 AZ × {public, app, data} subnets, IGW, per-AZ NAT, routes
│   ├── k8s-cluster/         kubeadm control-plane + worker ASGs, internal API NLB, node IAM
│   │                        (Bedrock invoke + ECR pull), KMS, deploy S3 bucket, kubectl-apply
│   │                        SSM document, SSM VPC endpoints, user-data templates
│   ├── alb-k8s/             internet ALB, :80→:443, instance target group on the ingress NodePort,
│   │                        /healthz:30254 health check, 300s deregistration delay
│   ├── ecr/                 demo-chat-server + demo-chat-client repos, scan-on-push, lifecycle
│   ├── github-oidc/         GitHub OIDC provider + deploy role (ECR push, secrets read, SSM send)
│   ├── rds-postgres/        RDS Postgres 16, subnet group, SG (5432 from app CIDRs)
│   ├── keyspaces/           Amazon Keyspaces keyspace + one modelled table (see caveat below)
│   ├── qdrant-ec2/          single EC2 Qdrant host + data EBS volume + SG (6333/6334 from app CIDRs)
│   ├── msk/                 MSK cluster + SG (9092/9094 from app CIDRs)
│   ├── ecs-service/         RETAINED REFERENCE — the old ECS Fargate path, not instantiated
│   ├── alb/                 RETAINED REFERENCE — ip target group for Fargate, not instantiated
│   └── bedrock-iam/         RETAINED REFERENCE — ECS task/exec roles, not instantiated
└── envs/
    ├── staging/             2 AZ, single-AZ RDS, 1 control-plane node, 2 workers
    └── prod/                3 AZ, multi-AZ RDS, 3 control-plane nodes, 3 workers — duplicated on purpose
```

`envs/staging` and `envs/prod` are **deliberately duplicated** rather than sharing a root module —
same reasoning as the duplicated `application-{staging,prod}.properties`.

## The env-var contract

Each env's `main.tf` builds a `plaintext_env` map and its `outputs.tf` exposes it as
`container_env`. Every key there must line up with a property in
`modules/server/src/main/resources/application-{staging,prod}.properties` **and** with the
`demo-chat-config` ConfigMap in `infra/k8s/manifest-{staging,prod}.yaml`:

| Container env var            | Source                                    |
|------------------------------|-------------------------------------------|
| `POSTGRES_HOST` / `_PORT` / `_DB` / `_USER` | `module.rds_postgres`             |
| `CASSANDRA_CONTACT_POINTS` / `_PORT` / `_LOCAL_DATACENTER` / `_KEYSPACE` | Keyspaces endpoint + `module.keyspaces` |
| `QDRANT_HOST`                | `module.qdrant`                           |
| `QDRANT_PORT` / `_USE_TLS` / `_INITIALIZE_SCHEMA` | constants (6334 / false / false)  |
| `KAFKA_BOOTSTRAP_SERVERS`    | `module.msk`                              |
| `POSTGRES_PASSWORD`, `CASSANDRA_USER`, `CASSANDRA_PASSWORD`, `QDRANT_API_KEY` | Secrets Manager via `task_secret_arns` — rendered into the k8s Secret by the deploy workflow, **not** in `container_env` |

`CASSANDRA_USER` joins the secret set (it was missing from the ECS contract — see
`docs/wiki/Daily/2026-08-31.md`): Amazon Keyspaces issues a service-specific username+password
pair, so both live in Secrets Manager.

`POSTGRES_SCHEMA` is intentionally **not** a variable: the app's Flyway `V1` migration hardcodes
schema `demo_chat`, so it is fixed, not configurable.

## Known gaps / TODO

- **State backend** — see the warning above. `backend "s3"` blocks commented out; CI runs
  `terraform init -backend=false`.
- **`node_ami_id`** — no default. The control-plane and worker nodes need an image with
  containerd + kubeadm/kubelet/kubectl and (workers) the `ecr-credential-provider` binary; the
  user-data expects `/opt/bootstrap/install-kube.sh` and
  `/opt/bootstrap/install-ecr-credential-provider.sh` baked in. See `infra/k8s/addons/README.md`.
- **`admin_cidr`, `github_org`** — no defaults; a bastion/VPN CIDR and the real repo owner.
- **kubeadm bootstrap is untested** — the SSM seed-election / join-command flow in
  `modules/k8s-cluster/templates/*.sh.tftpl` is written to shape, never run. `terraform validate`
  checks the `templatefile()` calls; nothing checks the shell logic beyond `shellcheck` on a
  rendered sample in the `manifests-lint` workflow.
- **GitHub OIDC provider is an account-global singleton** — both env roots pass
  `create_oidc_provider = true`. If staging and prod share one AWS account, set prod's to `false`
  and pass `existing_oidc_provider_arn`.
- **Keyspaces tables** — only `open_chats_by_bucket` is modelled. `chat_history` /
  `dialogue_state` use the `ChatMessage` UDT; port them (plus an `aws_keyspaces_type`) before an
  apply.
- **Data-tier ingress by CIDR, not SG** — `rds-postgres`, `qdrant-ec2`, `msk` allow the app
  subnet CIDRs. The worker SG id (`module.k8s_cluster.worker_security_group_id`) is now available
  to tighten these to SG references.
- **RDS password** — plain `variable`; switch to `manage_master_user_password` + Secrets Manager.
- **No `s3-cloudfront` module** — the client image is served from a pod, not S3/CloudFront.

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
