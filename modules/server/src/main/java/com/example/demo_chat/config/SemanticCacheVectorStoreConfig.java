package com.example.demo_chat.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A second pgvector table, separate from the {@code support_kb} knowledge base, used by {@code
 * SemanticCacheService} to cache previously-answered queries. Reuses the autoconfigured {@link
 * JdbcTemplate} and {@link EmbeddingModel} beans that back the primary (unqualified) knowledge-base
 * {@code VectorStore}.
 */
@Configuration
public class SemanticCacheVectorStoreConfig {

  @Bean
  @Qualifier("semanticCacheVectorStore")
  public VectorStore semanticCacheVectorStore(
      JdbcTemplate jdbcTemplate,
      EmbeddingModel embeddingModel,
      @Value("${demo-chat.cache.pgvector-table:semantic_cache}") String tableName,
      @Value("${spring.ai.vectorstore.pgvector.schema-name:demo_chat}") String schemaName,
      // Shares the knowledge-base store's setting: creating a table asks the embedding model for
      // its dimensions, which is a live Bedrock call, so an environment where tables are
      // provisioned ahead of time must be able to turn it off for both stores at once.
      @Value("${spring.ai.vectorstore.pgvector.initialize-schema:true}") boolean initializeSchema,
      // PgVectorStore also calls embeddingModel.dimensions() on every add()/similaritySearch() to
      // build observation/tracing metadata unless a dimension is pinned here — same live Bedrock
      // call as above, but on every turn instead of once at startup. Must mirror the autoconfigured
      // knowledge-base store's setting (left at -1, i.e. unset, in local/test/offline).
      @Value("${spring.ai.vectorstore.pgvector.dimensions:-1}") int dimensions) {
    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
        .schemaName(schemaName)
        .vectorTableName(tableName)
        .initializeSchema(initializeSchema)
        .dimensions(dimensions)
        .build();
  }
}
