# Local ↔ AWS: Component Mapping

[← Back to README](README.md) · [AWS infrastructure](infrastructure.md) · [Kubernetes layer](kubernetes.md)

**Status:** the local stack described here matches `modules/server/src/main/resources/local/docker-compose.yml` today.
Spring Profiles now exist (see below), so the *configuration* side of local ↔ AWS switching is done;
the AWS resources themselves are still planned — no Terraform yet.

## Mapping table

| Role | Local (docker-compose) | AWS (staging/prod — planned) |
|---|---|---|
| Vector store | Qdrant in a container | Qdrant on EC2/ECS (self-managed) or Amazon OpenSearch with vector engine |
| LLM inference | Amazon Bedrock by default; the `local,offline` profile swaps in a local **Ollama** (the `offline`-profile compose service via `make up-offline`, or a native install) so the app runs with no AWS | Amazon Bedrock (managed) |
| Chat history / dialogue state | Cassandra in a container | Amazon Keyspaces (Cassandra-compatible, managed) or self-managed Cassandra on EC2 |
| User accounts | Postgres in a container (R2DBC app access, JDBC/Flyway for migrations) | RDS for PostgreSQL |
| Application | Spring Boot in a container | Deployment on a self-managed **kubeadm cluster on EC2**, behind ingress-nginx + an ALB (see [kubernetes.md](kubernetes.md)) |
| Messaging | Kafka in a container | Amazon MSK (planned — not yet used by any code) |
| Object storage | not used yet | Amazon S3 (planned) |
| Secrets/config | `application-local.properties` (hardcoded dev values) | `application-{staging,prod}.properties`, every value from an env var — AWS Secrets Manager + Parameter Store supply them (planned) |
| Logs/metrics | stdout + docker logs, Actuator endpoints | CloudWatch Logs + CloudWatch Metrics (planned) |

There is no Redis anywhere in this project's dependency set — it was part of an earlier draft of this
plan and has been superseded by Cassandra (chat history). Ollama is not a dependency either, but the
`local,offline` profile does talk to one at runtime as a no-AWS substitute for Bedrock (chat +
embeddings); Bedrock stays the default and the only production provider.

## Switching via Spring Profiles (implemented)

```
SPRING_PROFILES_ACTIVE unset        → application-local.properties    (docker-compose stack)
SPRING_PROFILES_ACTIVE=local,offline → + application-offline.properties (Ollama instead of Bedrock, no AWS)
SPRING_PROFILES_ACTIVE=staging      → application-staging.properties  (managed AWS services)
SPRING_PROFILES_ACTIVE=prod         → application-prod.properties
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
├── kafka      (port 9092)       — declared, still not used by any code
└── ollama     (port 11434)      — only under the `offline` Compose profile (`make up-offline`); chat +
                                   embedding provider for the `local,offline` Spring profile. Needs
                                   ~5 GB RAM in the VM for `llama3.1` on top of the four services
                                   above — the Docker VM must be >= ~12 GiB (Colima defaults to 2).
```

The application itself is not part of this compose file (it's run separately via `./gradlew :server:bootRun`),
and there is no LocalStack/S3/SQS emulation and no `app` service — both appeared in an earlier draft of
this document but don't reflect the current setup. The `ollama` service is real but profile-gated, so a
plain `make up` still starts only the four services above it.

## Related documents

- [AWS infrastructure](infrastructure.md) — planned
- [Java backend structure](backend.md)