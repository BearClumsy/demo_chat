# Local ↔ AWS: Component Mapping

[← Back to README](README.md) · [AWS infrastructure](infrastructure.md)

**Status:** the local stack described here matches `modules/server/src/main/resources/local/docker-compose.yml` today.
Spring Profiles now exist (see below), so the *configuration* side of local ↔ AWS switching is done;
the AWS resources themselves are still planned — no Terraform yet.

## Mapping table

| Role | Local (docker-compose) | AWS (staging/prod — planned) |
|---|---|---|
| Vector store | Qdrant in a container | Qdrant on EC2/ECS (self-managed) or Amazon OpenSearch with vector engine |
| LLM inference | Amazon Bedrock (no local/offline provider — there's no Ollama or other local-model dependency in this project) | Amazon Bedrock (managed) |
| Chat history / dialogue state | Cassandra in a container | Amazon Keyspaces (Cassandra-compatible, managed) or self-managed Cassandra on EC2 |
| User accounts | Postgres in a container (R2DBC app access, JDBC/Flyway for migrations) | RDS for PostgreSQL |
| Application | Spring Boot in a container | ECS Fargate (or EKS), behind an ALB |
| Messaging | Kafka in a container | Amazon MSK (planned — not yet used by any code) |
| Object storage | not used yet | Amazon S3 (planned) |
| Secrets/config | `application-local.properties` (hardcoded dev values) | `application-{staging,prod}.properties`, every value from an env var — AWS Secrets Manager + Parameter Store supply them (planned) |
| Logs/metrics | stdout + docker logs, Actuator endpoints | CloudWatch Logs + CloudWatch Metrics (planned) |

There is no Redis and no Ollama anywhere in this project's dependency set — those were part of an
earlier draft of this plan and have been superseded by Cassandra (chat history) and Bedrock-only (LLM).

## Switching via Spring Profiles (implemented)

```
SPRING_PROFILES_ACTIVE unset     → application-local.properties   (docker-compose stack)
SPRING_PROFILES_ACTIVE=staging   → application-staging.properties (managed AWS services)
SPRING_PROFILES_ACTIVE=prod      → application-prod.properties
```

`application.properties` keeps only what doesn't vary by environment — Bedrock model ids, Qdrant
collection names, the `demo-chat.*` tuning knobs — plus `spring.profiles.default=local`, so
`./gradlew :server:bootRun` and IDE runs behave exactly as they did before profiles existed.

No code changed to make this work: the app already depended on Spring Data/Spring AI interfaces
(`ReactiveCassandraRepository`, `R2dbcRepository`, `ChatModel`/`VectorStore`) rather than concrete
implementations, so switching environments is purely a matter of properties.

Two things worth knowing about the staging/prod files:

- **Every value is an environment variable, and secrets have no default** —
  `spring.r2dbc.password=${POSTGRES_PASSWORD}` fails startup if unset, rather than quietly falling
  back to the local dev password. Values with a safe default (ports, pool sizes, `POSTGRES_DB`) keep
  one.
- **They duplicate each other rather than importing a shared file.** A shared
  `aws-common.properties` pulled in with `spring.config.import` was tried first and did not take
  effect from a profile-specific document — the app silently fell back to Boot's defaults
  (`127.0.0.1:9042` for Cassandra). Two explicit files are easier to trust, and staging/prod diverge
  on pool sizes and log levels anyway.

`spring.ai.vectorstore.qdrant.initialize-schema` now governs **both** Qdrant stores (`support_kb` and
`semantic_cache`); the semantic-cache store used to hardcode it to `true`. This matters because
creating a collection asks the embedding model for its dimensions, which is a live Bedrock call —
staging/prod default it to `false` and expect collections to be provisioned before the deploy.

## docker-compose (local stack, actual)

```
modules/server/src/main/resources/local/docker-compose.yml
├── postgres   (port 5432)  — users table (R2DBC app access, JDBC/Flyway for migrations)
├── cassandra  (port 9042)  — chat_history + dialogue_state tables
├── qdrant     (ports 6333/6334) — support_kb (knowledge base) + semantic_cache (Phase 2) collections
└── kafka      (port 9092)       — declared, still not used by any code
```

The application itself is not part of this compose file (it's run separately via `./gradlew :server:bootRun`),
and there is no LocalStack/S3/SQS emulation, no Ollama entry, and no `app` service — all of which appear
in the earlier draft of this document but don't reflect the current setup.

## Related documents

- [AWS infrastructure](infrastructure.md) — planned
- [Java backend structure](backend.md)