# AWS Infrastructure

[← Back to README](README.md) · [Local ↔ AWS mapping](local-vs-aws.md) · [Kubernetes layer](kubernetes.md)

> **Compute pivot (2026-09-03).** The app now deploys to **self-managed Kubernetes (kubeadm) on
> EC2** behind the NGINX Ingress Controller, not ECS Fargate — see [kubernetes.md](kubernetes.md).
> The Terraform gained `modules/{k8s-cluster, alb-k8s, ecr, github-oidc}` and the `envs/*` were
> rewired; `ecs-service`, `alb` and `bedrock-iam` are retained lint-clean but no longer
> instantiated. The ECS-shaped diagram and notes below are kept as background; the "ECS Fargate"
> rows now read as "kubeadm worker nodes".

**Status:** planned, not yet provisioned. A **lint-only Terraform skeleton now exists** under
`infra/terraform/`, CI-linted by the `terraform-lint` workflow (`fmt` + `validate` + `tflint`, no
AWS credentials). Nothing is applied: there is no AWS account, `terraform plan`/`apply` have never
run, the S3/DynamoDB state backend is commented out, and AWS-specific values (AMI ids, ACM cert
ARNs, Secrets Manager ARNs, `github_org`, `admin_cidr`) are `TODO` variables. `infra/terraform/README.md`
tracks what the skeleton does and does not cover yet.

## Diagram (high level)

```
                            ┌─────────────────────────┐
                            │        Route 53          │
                            └────────────┬─────────────┘
                                         ▼
                            ┌─────────────────────────┐
                            │    CloudFront (frontend)  │
                            └────────────┬─────────────┘
                                         ▼
                            ┌─────────────────────────┐
                            │   S3 (React static build) │
                            └─────────────────────────┘

                            ┌─────────────────────────┐
                            │            ALB            │
                            └────────────┬─────────────┘
                                         ▼
                     ┌───────────────────────────────────┐
                     │   ECS Fargate Service (Spring Boot)  │
                     │   auto-scaling by CPU/latency/RPS    │
                     └───┬────────────┬────────────┬───┬───┘
                         ▼            ▼            ▼   ▼
               ┌─────────────┐ ┌────────────┐ ┌──────────────┐ ┌──────────────┐
               │  Qdrant on   │ │ Cassandra   │ │   Bedrock     │ │   RDS         │
               │  EC2/ECS     │ │ (Keyspaces  │ │  (LLM/Embed)  │ │  (Postgres,   │
               │              │ │ or self-mgd)│ │               │ │  users)       │
               └─────────────┘ └────────────┘ └──────────────┘ └──────────────┘

         ┌────────────────┐   ┌───────────────┐   ┌──────────────────┐
         │  Secrets Manager │   │ CloudWatch     │   │  S3 (docs/backup) │
         └────────────────┘   └───────────────┘   └──────────────────┘
```

## Network layout (VPC)

```
VPC
├── Public Subnets (2+ AZ)
│   └── ALB, NAT Gateway
├── Private Subnets — app layer (2+ AZ)
│   └── ECS Fargate tasks (Spring Boot)
└── Private Subnets — data layer (2+ AZ)
    ├── Qdrant (EC2 or ECS with EBS/EFS for persistence)
    ├── Cassandra (Amazon Keyspaces, managed — or self-managed on EC2/ECS)
    └── RDS for PostgreSQL (Multi-AZ) — users table, Flyway-managed
```

## Core AWS services and their roles

| Service | Role |
|---|---|
| **ECS Fargate** | Hosts the Spring Boot application, auto-scaling |
| **ALB** | Load balancing + SSE-compatible long-lived connection routing |
| **Amazon Bedrock** | Managed LLM (generation, intent classification) and embedding models |
| **Qdrant (self-managed on EC2/ECS)** | Vector store for topics/answers |
| **Amazon Keyspaces (Cassandra-compatible)** or self-managed Cassandra on EC2 | Chat history / dialogue state, matching the local Cassandra store |
| **RDS for PostgreSQL** | User accounts (R2DBC for app access, Flyway/JDBC for migrations only — see [[users]]) |
| **S3** | React app static assets, knowledge base backups, documents for reindexing |
| **CloudFront** | CDN for the frontend |
| **Secrets Manager / Parameter Store** | Keys, endpoints, per-environment config |
| **CloudWatch** | Logs, metrics, alarms (LLM latency, retrieval errors) |
| **ECR** | Docker images for backend/frontend |
| **IAM** | Roles for ECS tasks (access to Bedrock, Secrets Manager, S3) |
| **WAF** (optional) | Protects the ALB from abusive traffic |

## Scaling for 500K+ users

- **ECS Service Auto Scaling** — driven by target latency and RPS, not just CPU.
- **Bedrock** removes the need to scale GPU inference yourself.
- **Qdrant** — as load grows: shard the collection or move to a managed vector engine (Amazon
  OpenSearch) to offload operational burden.
- **Cassandra** — Amazon Keyspaces scales on-demand capacity automatically; if self-managed, scale by
  adding nodes/expanding the ring.
- **RDS for PostgreSQL** — read replicas if user-lookup read load grows.
- **Read caching / hot-intent caching** — implemented as of Phase 2: `SemanticCacheService` caches
  guardrail-validated answers in a second Qdrant collection (`semantic_cache`), matched by semantic
  similarity of the normalized query, short-circuiting retrieval/classification/generation on a hit. Not
  Cassandra, as earlier drafts of this doc left open — see [[semantic_cache]] and
  [vector-store-schema.md](vector-store-schema.md).

## Terraform structure (IaC)

The lint-only skeleton that now exists (`[~]` in [roadmap.md](roadmap.md) Phase 3b):

```
infra/terraform/
├── .tflint.hcl                   # terraform + aws rulesets
├── README.md                     # what the skeleton covers / what is still TODO
├── modules/
│   ├── vpc/                       # 2–3 AZ × {public, app, data} subnets, per-AZ NAT
│   ├── alb/                       # :80→:443 redirect, HTTPS listener, ip target group
│   ├── ecs-service/               # cluster + Fargate service + task def + task SG + log group
│   ├── rds-postgres/
│   ├── keyspaces/                 # keyspace + open_chats_by_bucket only (see README caveat)
│   ├── qdrant-ec2/               # single EC2 host + data EBS volume
│   ├── msk/                       # added — the KAFKA_BOOTSTRAP_SERVERS contract needs it
│   └── bedrock-iam/               # task role (InvokeModel) + execution role
├── envs/
│   ├── staging/                   # versions.tf, main.tf, variables.tf, outputs.tf, terraform.tfvars
│   └── prod/                      # duplicated from staging on purpose (as with the *.properties)
```

Each env's `versions.tf` carries the `backend "s3"` block **commented out** (S3 + DynamoDB lock)
until the state bucket exists; CI runs `terraform init -backend=false`. Not yet built, vs. the
original target above: `s3-cloudfront/` (React static hosting) and a `cassandra-ec2/` alternative
to `keyspaces/`. The full `chat_history` / `dialogue_state` Keyspaces tables (they use the
`ChatMessage` UDT) still need porting from the JPA entities / `db/cassandra/*.cql`.

## Related documents

- [Local ↔ AWS mapping](local-vs-aws.md)
- [GitHub Actions (deploy to ECS)](github-actions.md) — planned