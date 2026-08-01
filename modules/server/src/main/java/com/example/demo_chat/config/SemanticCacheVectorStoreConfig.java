package com.example.demo_chat.config;

import io.qdrant.client.QdrantClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A second Qdrant collection, separate from the {@code support_kb} knowledge base, used by {@code
 * SemanticCacheService} to cache previously-answered queries. Reuses the autoconfigured {@link
 * QdrantClient} and {@link EmbeddingModel} beans that back the primary (unqualified) knowledge-base
 * {@code VectorStore}.
 */
@Configuration
public class SemanticCacheVectorStoreConfig {

  @Bean
  @Qualifier("semanticCacheVectorStore")
  public VectorStore semanticCacheVectorStore(
      QdrantClient qdrantClient,
      EmbeddingModel embeddingModel,
      @Value("${demo-chat.cache.qdrant-collection:semantic_cache}") String collectionName,
      // Shares the knowledge-base store's setting: creating a collection asks the embedding model
      // for its dimensions, which is a live Bedrock call, so an environment where collections are
      // provisioned ahead of time must be able to turn it off for both stores at once.
      @Value("${spring.ai.vectorstore.qdrant.initialize-schema:true}") boolean initializeSchema) {
    return QdrantVectorStore.builder(qdrantClient, embeddingModel)
        .collectionName(collectionName)
        .initializeSchema(initializeSchema)
        .build();
  }
}
