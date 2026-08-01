# Running Locally

[← Back to README](README.md) · [Local ↔ AWS mapping](local-vs-aws.md)

**Status:** current as of Phase 3a (Spring Profiles + containers + CI). Every command here was run on
macOS with Colima; the only machine-specific parts are called out.

## Prerequisites

| Need | Notes |
|---|---|
| Docker | Postgres, Cassandra, Qdrant, and Kafka all run as containers. Tests need it too. |
| Java 26 | Provisioned by the Gradle toolchain — use `./gradlew`, don't rely on the system JDK. |
| Node 22+ | For the React client and `scripts/validate-intents.mjs`. |
| AWS credentials with Bedrock access | Required to *start* the app, not just to chat — see step 3. |

## 1. Start the dependencies

```bash
docker compose -f modules/server/src/main/resources/local/docker-compose.yml up -d
```

Cassandra takes 60–90 seconds to become healthy; wait for `(healthy)` in `docker ps` before starting
the app. Its heap is capped in the compose file (`MAX_HEAP_SIZE`/`HEAP_NEWSIZE`) — if it exits with
code 137 it was OOM-killed, so give the Docker VM more memory.

## 2. Create the Cassandra keyspace (one-time)

```bash
docker exec local-cassandra-1 cqlsh -e \
  "CREATE KEYSPACE IF NOT EXISTS demo_chat WITH replication = {'class':'SimpleStrategy','replication_factor':1}"
```

`spring.cassandra.schema-action=create-if-not-exists` creates *tables*, never the keyspace, and Spring
Data connects to an existing one. Skip this and startup fails with `Could not reach any contact point`,
which reads like a connectivity problem but isn't. The volume persists, so this is needed once per
`docker compose down -v`.

## 3. Export AWS credentials

```bash
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
```

These are needed at **startup**, not only per chat turn: on first boot the app creates the `support_kb`
and `semantic_cache` Qdrant collections, and creating a collection asks the embedding model for its
dimensions — a live Bedrock call. `KnowledgeBaseIndexer` then embeds every intent
(`demo-chat.rag.reindex-on-startup=true` under `local`).

**Without Bedrock access** the backend can still be booted, but no chat will work:

```bash
./gradlew :server:bootRun --args='--spring.ai.vectorstore.qdrant.initialize-schema=false --demo-chat.rag.reindex-on-startup=false'
```

Useful for working on auth, the HTTP layer, or the frontend shell. Any message sent to the pipeline
fails at the first Bedrock call.

## 4. Run the backend

```bash
./gradlew :server:bootRun
```

No profile needs to be set: `application.properties` declares `spring.profiles.default=local`, and
`application-local.properties` holds the `localhost` connection details matching the compose stack.
The log line to look for is `No active profile set, falling back to 1 default profile: "local"`.

Listening on `:8080`. `/actuator/health` is the only endpoint that permits unauthenticated access:

```bash
curl localhost:8080/actuator/health          # {"status":"UP"}
curl -o /dev/null -w '%{http_code}\n' localhost:8080/actuator/metrics   # 401 — auth required
```

## 5. Run the frontend

```bash
cd modules/client
npm install      # first time only
npm run dev
```

Open http://localhost:5173. The Vite dev server proxies `/api` to `http://localhost:8080`
(`vite.config.ts`), so no CORS configuration is involved. Sign up in the UI, then start a chat — see
[frontend-chat-mvp.md](frontend-chat-mvp.md) for what the MVP does and doesn't cover.

## Exercising the API directly

Registration is the one unauthenticated endpoint; everything after it uses HTTP Basic.

```bash
# 1. create a user (returns the user's id)
curl -s -X POST localhost:8080/api/users -H 'Content-Type: application/json' -d '{
  "firstName":"Ada","lastName":"Lovelace","email":"ada@example.com",
  "login":"ada","password":"hunter2hunter2"
}'

# 2. start a chat (participantIds must be real user ids)
curl -s -u ada:hunter2hunter2 -X POST localhost:8080/api/chats \
  -H 'Content-Type: application/json' -d '{
  "currentUserId":"<user-id>","participantIds":["<user-id>"],"title":"Support",
  "message":{"userId":"<user-id>","message":"Where is my order?","datetime":"2026-08-01T12:00:00Z"}
}'

# 3. send a message and stream the reply
curl -N -u ada:hunter2hunter2 -X POST localhost:8080/api/chats/<chat-id>/messages/stream \
  -H 'Content-Type: application/json' -d '{"message":"Where is my order 12345?"}'
```

The stream emits `token` events followed by one `done` event — buffer-then-chunk, not live token
generation, so the guardrail can run before anything reaches the client.

## Running from the container images

The `local` profile hardcodes `localhost`, which is wrong inside a container. Use the `staging` profile
instead and point every host at the compose containers — this is also the quickest way to sanity-check
that the staging configuration binds correctly before there's an AWS environment to try it on.

```bash
docker build -f modules/server/Dockerfile -t demo-chat-server .   # context is the repo ROOT
docker run --rm --network local_default -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e POSTGRES_HOST=local-postgres-1 -e POSTGRES_USER=demo_chat -e POSTGRES_PASSWORD=demo_chat \
  -e CASSANDRA_CONTACT_POINTS=local-cassandra-1 -e CASSANDRA_LOCAL_DATACENTER=datacenter1 \
  -e CASSANDRA_USER=cassandra -e CASSANDRA_PASSWORD=cassandra \
  -e QDRANT_HOST=local-qdrant-1 -e QDRANT_USE_TLS=false -e QDRANT_API_KEY= \
  -e QDRANT_INITIALIZE_SCHEMA=false \
  -e KAFKA_BOOTSTRAP_SERVERS=local-kafka-1:9092 \
  -e AWS_REGION -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY \
  demo-chat-server
```

Container **names** (`local-postgres-1`) rather than compose **service** names (`postgres`): service-name
DNS did not resolve from a container attached to `local_default` on Colima.

The client image is a static nginx build with an `/api` proxy, so it needs to know where the backend is:

```bash
docker build -t demo-chat-client modules/client
docker run --rm -p 8081:80 -e BACKEND_URL=http://host.docker.internal:8080 demo-chat-client
```

nginx resolves the upstream at startup, so an unreachable `BACKEND_URL` makes the container exit
immediately with `host not found in upstream` rather than starting and failing per request.

## Running the tests

```bash
./gradlew :server:build          # spotless + all 8 test classes
```

Needs Docker but **not** AWS credentials, and **not** the compose stack — Testcontainers starts its own
Postgres, Cassandra, and Qdrant, and `application-test.properties` switches Bedrock off
(`spring.ai.model.chat=none` / `spring.ai.model.embedding=none`) in favour of stub beans. Expect
~90 seconds, most of it Cassandra starting.

If the Docker daemon isn't at the default socket (Colima, Rancher Desktop), Testcontainers can't find
it and fails in `DockerClientProviderStrategy`:

```bash
export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
```

Other checks that CI also runs:

```bash
node scripts/validate-intents.mjs            # knowledge-base intent files
cd modules/client && npm run lint            # ESLint
```

## Troubleshooting

| Symptom | Cause |
|---|---|
| `Could not reach any contact point ... 9042` | The `demo_chat` keyspace doesn't exist — step 2. |
| Cassandra container `Exited (137)` | OOM-killed; raise the Docker VM's memory. |
| `Unable to load region from any of the providers` at startup | No `AWS_REGION`/credentials, and the Qdrant collections don't exist yet — step 3. |
| Startup reaches Bedrock and gets 403 | Credentials resolve but lack Bedrock model access. |
| `DockerClientProviderStrategy` failure in tests | Non-default Docker socket — set `DOCKER_HOST`. |
| Backend up, frontend shows network errors | The dev server proxies `/api` to `:8080`; check the backend is actually on that port. |

## Related documents

- [Local ↔ AWS: component mapping](local-vs-aws.md) — what each local container becomes in AWS, and how
  the profiles switch between them
- [Frontend Chat MVP](frontend-chat-mvp.md)
- [GitHub Actions](github-actions.md) — the same checks, run in CI
- [Phased implementation roadmap](roadmap.md)
