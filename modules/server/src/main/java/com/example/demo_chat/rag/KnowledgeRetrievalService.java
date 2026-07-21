package com.example.demo_chat.rag;

import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Stage 2 of the RAG pipeline: retrieves the top-K candidate intents from the Vector Store. */
@Service
public class KnowledgeRetrievalService {

  private final VectorStore vectorStore;
  private final int topK;

  public KnowledgeRetrievalService(
      VectorStore vectorStore, @Value("${demo-chat.rag.top-k:3}") int topK) {
    this.vectorStore = vectorStore;
    this.topK = topK;
  }

  /**
   * @param normalizedQuery the normalized user query
   * @return the top-K most similar knowledge base documents
   */
  public Mono<List<Document>> retrieve(String normalizedQuery) {
    return Mono.fromCallable(
            () ->
                vectorStore.similaritySearch(
                    SearchRequest.builder().query(normalizedQuery).topK(topK).build()))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
