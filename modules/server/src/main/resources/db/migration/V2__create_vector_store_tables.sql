-- Mirrors exactly what Spring AI's PgVectorStore#initializeSchema would create (verified against
-- the resolved spring-ai-pgvector-store:2.0.0 jar), so that turning off
-- spring.ai.vectorstore.pgvector.initialize-schema in staging/prod (schema is Flyway-managed there)
-- produces byte-for-byte the same objects PgVectorStore would create itself in local/test/offline.
-- Installed into `public` explicitly: Flyway sets search_path to just `demo_chat` while migrating
-- (spring.flyway.schemas=demo_chat), so an unqualified CREATE EXTENSION here would install pgvector's
-- operator classes into `demo_chat`, invisible to PgVectorStore's own JDBC connection later (default
-- search_path "$user",public) and breaking `USING hnsw (embedding vector_cosine_ops)`.
-- Explicit, rather than relying on Flyway's default search_path for this session: guarantees the
-- `vector` type and `uuid_generate_v4()` function below resolve regardless of Flyway internals.
SET search_path TO demo_chat, public;

CREATE EXTENSION IF NOT EXISTS vector SCHEMA public;
CREATE EXTENSION IF NOT EXISTS hstore SCHEMA public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;

-- Bedrock Titan v2 (amazon.titan-embed-text-v2:0) embeds at 1024 dimensions; the offline profile's
-- Ollama nomic-embed-text embeds at 768 and is incompatible with this fixed column width, exactly
-- as it was already incompatible with the Qdrant collection's dimension - see the CLAUDE.md gotcha.
CREATE TABLE demo_chat.support_kb (
    id        uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content   text,
    metadata  json,
    embedding vector(1024)
);
CREATE INDEX support_kb_index ON demo_chat.support_kb USING hnsw (embedding vector_cosine_ops);

CREATE TABLE demo_chat.semantic_cache (
    id        uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content   text,
    metadata  json,
    embedding vector(1024)
);
CREATE INDEX semantic_cache_index ON demo_chat.semantic_cache USING hnsw (embedding vector_cosine_ops);
