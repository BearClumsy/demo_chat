# AWS Infrastructure

[← Back to README](README.md) · [Local ↔ AWS mapping](local-vs-aws.md)

**Status:** planned, not yet provisioned — no Terraform, AWS resources, or `infra/` directory exist in
this repo yet. Everything below is target design. Updated only to swap the outdated Redis/ElastiCache
references for the project's actual chat-state store (Cassandra) — the AWS deployment itself is still
future work, same as [github-actions.md](github-actions.md).

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

```
infra/terraform/
├── modules/
│   ├── vpc/
│   ├── ecs-service/
│   ├── alb/
│   ├── rds-postgres/
│   ├── keyspaces/                 # or cassandra-ec2/ for self-managed
│   ├── qdrant-ec2/
│   ├── bedrock-iam/
│   └── s3-cloudfront/
├── envs/
│   ├── staging/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   └── prod/
│       ├── main.tf
│       ├── variables.tf
│       └── terraform.tfvars
└── backend.tf                 # S3 + DynamoDB lock for state
```

## Related documents

- [Local ↔ AWS mapping](local-vs-aws.md)
- [GitHub Actions (deploy to ECS)](github-actions.md) — planned