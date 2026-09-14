# Postgres Table: users

**Migration:** `V1__create_users_table.sql`

## Columns

- `id` — `UUID`, PK, generated (`gen_random_uuid()` at the DB level as a default; app-side inserts leave
  `id` unset and rely on `INSERT ... RETURNING *` to get it back — see Notes)
- `first_name`, `last_name` — `VARCHAR(100)`, not null
- `email` — `VARCHAR(255)`, not null, **unique** (used as a login identifier)
- `phone` — `VARCHAR(20)`, nullable
- `login` — `VARCHAR(100)`, not null, **unique** (used as a login identifier)
- `password` — `VARCHAR(255)`, not null (bcrypt hash, never plaintext — see [[User]])

Lives in the `demo_chat` schema (not `public` — see Notes).

## Used By

- [[User]] — reads via `GET /api/users/{id}`, writes via `POST /api/users`
- [[Chat]] — `ChatService.validateParticipantIds()` looks up participant ids here before starting a chat
  or adding a participant

## Notes

- Flyway is pinned to this schema explicitly (`spring.flyway.schemas=demo_chat` in
  `application.properties`). Without it, Postgres's default `search_path` (`"$user", public`) silently
  resolves to the `demo_chat` schema once it exists, because the DB role is also named `demo_chat` —
  this desynced Flyway's history table from the schema it manages and caused real migration failures
  during development. Don't remove that property without understanding this.
- `email` and `login` are both unique because either can be used to identify a user; there's no
  single canonical "username" field.
- **Access is R2DBC, not JPA** (migrated in Phase 2 — see `docs/wiki/Plan/roadmap.md`):
  `UserRepository extends R2dbcRepository<User, UUID>`, called directly from `UserService`/
  `SecurityUserDetailsService`/`ChatService` with no `Schedulers.boundedElastic()` bridge. Postgres also
  has a **separate blocking JDBC `DataSource`** (`spring.datasource.*` in `application.properties`,
  alongside `spring.r2dbc.*` for the app) — originally added just so Flyway could run migrations, and
  now, since the pgvector migration (`docs/wiki/Plan/postgres-vector-migration.md`), also used by the
  `support_kb`/`semantic_cache` `PgVectorStore` beans (via a `JdbcTemplate`). The `DataSource`/
  `JdbcTemplate` beans are declared explicitly in `config/JdbcDataSourceConfig.java` because Boot's own
  `DataSourceAutoConfiguration` backs off whenever an R2DBC `ConnectionFactory` bean is present. The two
  connection configs point at the same database but serve different purposes; don't assume removing one
  is safe without checking the other still works.
- The R2DBC connection URL carries `?schema=demo_chat` (`spring.r2dbc.url`) instead of a JPA
  `@Table(schema=...)` annotation, since `User.java` now uses an unqualified
  `@Table("users")` (Spring Data Relational).
