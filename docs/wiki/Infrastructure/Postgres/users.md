# Postgres Table: users

**Migration:** `V1__create_users_table.sql`

## Columns

- `id` — `UUID`, PK, generated (`gen_random_uuid()` at the DB level as a default; app-side inserts via
  JPA use Hibernate's `GenerationType.UUID` instead)
- `first_name`, `last_name` — `VARCHAR(100)`, not null
- `email` — `VARCHAR(255)`, not null, **unique** (used as a login identifier)
- `phone` — `VARCHAR(20)`, nullable
- `login` — `VARCHAR(100)`, not null, **unique** (used as a login identifier)
- `password` — `VARCHAR(255)`, not null (bcrypt hash, never plaintext — see [[User]])

Lives in the `demo_chat` schema (not `public` — see Notes).

## Used By

- [[User]] — reads via `GET /api/users/{id}`, writes via `POST /api/users`

## Notes

- Flyway is pinned to this schema explicitly (`spring.flyway.schemas=demo_chat` in
  `application.properties`). Without it, Postgres's default `search_path` (`"$user", public`) silently
  resolves to the `demo_chat` schema once it exists, because the DB role is also named `demo_chat` —
  this desynced Flyway's history table from the schema it manages and caused real migration failures
  during development. Don't remove that property without understanding this.
- `email` and `login` are both unique because either can be used to identify a user; there's no
  single canonical "username" field.
