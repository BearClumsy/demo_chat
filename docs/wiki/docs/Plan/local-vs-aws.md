# Local ↔ AWS: Component Mapping

[← Back to README](README.md) · [AWS infrastructure](infrastructure.md)

**Status:** the local stack described here matches `src/main/resources/local/docker-compose.yml` today.
The AWS/staging/prod side is still planned — no Terraform or Spring Profiles exist yet (see below).

## Mapping table

| Role | Local (docker-compose) | AWS (staging/prod — planned) |
|---|---|---|
| Vector store | Qdrant in a container | Qdrant on EC2/ECS (self-managed) or Amazon OpenSearch with vector engine |
| LLM inference | Amazon Bedrock (no local/offline provider — there's no Ollama or other local-model dependency in this project) | Amazon Bedrock (managed) |
| Chat history / dialogue state | Cassandra in a container | Amazon Keyspaces (Cassandra-compatible, managed) or self-managed Cassandra on EC2 |
| User accounts | Postgres in a container (Flyway-managed schema) | RDS for PostgreSQL |
| Application | Spring Boot in a container | ECS Fargate (or EKS), behind an ALB |
| Messaging | Kafka in a container | Amazon MSK (planned — not yet used by any code) |
| Object storage | not used yet | Amazon S3 (planned) |
| Secrets/config | `application.properties` (single file, no profiles yet) | AWS Secrets Manager + Parameter Store (planned) |
| Logs/metrics | stdout + docker logs, Actuator endpoints | CloudWatch Logs + CloudWatch Metrics (planned) |

There is no Redis and no Ollama anywhere in this project's dependency set — those were part of an
earlier draft of this plan and have been superseded by Cassandra (chat history) and Bedrock-only (LLM).

## Switching via Spring Profiles (planned — not implemented yet)

Currently there is a single `application.properties` with hardcoded `localhost` connection settings for
Postgres, Cassandra, Bedrock, Qdrant, and Kafka — there are no `application-local.properties`,
`application-staging.properties`, or `application-prod.properties` files, and no
`SPRING_PROFILES_ACTIVE` switching yet. The target design remains:

```
SPRING_PROFILES_ACTIVE=local     → application-local.properties   (docker-compose stack)
SPRING_PROFILES_ACTIVE=staging   → application-staging.properties (managed AWS services)
SPRING_PROFILES_ACTIVE=prod      → application-prod.properties
```

Precondition (already true today): code depends on Spring Data/Spring AI interfaces
(`ReactiveCassandraRepository`, `ChatModel`/`VectorStore` once RAG is wired) rather than a specific
implementation, which is what will allow local ↔ AWS switching to be purely config-driven once profiles
are introduced.

## docker-compose (local stack, actual)

```
src/main/resources/local/docker-compose.yml
├── postgres   (port 5432)  — users table, Flyway-migrated
├── cassandra  (port 9042)  — chat_history table
├── qdrant     (ports 6333/6334) — declared, not yet used by any code
└── kafka      (port 9092)       — declared, not yet used by any code
```

The application itself is not part of this compose file (it's run separately via `./gradlew bootRun`),
and there is no LocalStack/S3/SQS emulation, no Ollama entry, and no `app` service — all of which appear
in the earlier draft of this document but don't reflect the current setup.

## Related documents

- [AWS infrastructure](infrastructure.md) — planned
- [Java backend structure](backend.md)