# demo_chat Wiki

Project knowledge base — requirements, decisions, and notes from meetings and docs. Open `docs/wiki/`
as an Obsidian vault; navigate via links, not folders.

This is manually/AI-curated project knowledge, distinct from `graphify-out/`, which is an
auto-generated code graph of what's actually implemented.

## Structure

- **[[Features]]** — one note per feature (`Features/<name>.md`). Product/behavior-level: what it does,
  requirements, decisions, open questions.
- **Infrastructure** — one note per concrete technical resource, grouped by system. Feature notes link
  into these instead of restating config inline:
  - `Infrastructure/Kafka/<topic-name>.md` — one note per topic (partitions, key, schema, producers, consumers)
  - `Infrastructure/Postgres/<table-name>.md` — one note per table (schema, migration file, columns)
  - `Infrastructure/Cassandra/<table-name>.md` — one note per table (partition/clustering keys, TTL)
  - `Infrastructure/Qdrant/<collection-name>.md` — one note per collection (embedding model, dims, distance metric)
- **Plan** — `Plan/<topic>.md`, architecture/roadmap-level rather than per-resource: system overview,
  RAG pipeline design, phased roadmap, AWS/CI-CD plans. `Plan/README.md` is its own entry point/index
  (same role as this file, one level down); `Plan/roadmap.md` tracks what's actually implemented per
  phase.

Each folder has a `_template.md` — copy it when creating a new note, then delete the template comment
at the top. (`Plan/` doesn't use this template pattern — its notes are one-off architecture topics, not
repeated per-resource entries.)

## Linking convention

Use `[[Note Name]]` wikilinks. A feature note links out to every infra resource it touches; each infra
note lists the features that use it back (manual "Used by" section — don't rely on Obsidian's graph
view alone since these docs may also be read outside Obsidian).

## Map of Content

### Features

- [[Chat]]
- [[User]]

### Infrastructure

- [[chat_history]] (Cassandra)
- [[dialogue_state]] (Cassandra)
- [[users]] (Postgres)
- [[support_kb]] (Qdrant)
- [[semantic_cache]] (Qdrant)

### Plan

- [[README|Plan overview]]
- [[roadmap]]

### Daily

- [[2026-07-13]]
- [[2026-07-14]]
- [[2026-07-15]]