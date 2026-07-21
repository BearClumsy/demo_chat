package com.example.demo_chat.rag;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.test.StepVerifier;

/**
 * Unit-tests {@link SemanticCacheService} against a hand-built {@link VectorStore} fake, since a
 * real similarity lookup would need a live embedding model (Bedrock Titan) that isn't reachable in
 * this environment. The real Qdrant round-trip is covered by a manual smoke test against the local
 * docker-compose stack instead (see the plan's verification section).
 */
class SemanticCacheServiceTest {

  private final VectorStore vectorStore = mock(VectorStore.class);

  @Test
  void lookupReturnsTheCachedAnswerOnAHit() {
    Document cached =
        Document.builder().text("where is my refund").metadata("answer", "3-5 days").build();
    when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(cached));

    new SemanticCacheService(vectorStore, 0.95, true)
        .lookup("where is my refund")
        .as(StepVerifier::create)
        .expectNext("3-5 days")
        .verifyComplete();
  }

  @Test
  void lookupReturnsEmptyWhenNoDocumentMatches() {
    when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

    new SemanticCacheService(vectorStore, 0.95, true)
        .lookup("some unrelated query")
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  void lookupSkipsTheVectorStoreWhenDisabled() {
    new SemanticCacheService(vectorStore, 0.95, false)
        .lookup("anything")
        .as(StepVerifier::create)
        .verifyComplete();

    verify(vectorStore, never()).similaritySearch(any(SearchRequest.class));
  }

  @Test
  void storeAddsADocumentWithTheAnswerAsMetadata() {
    new SemanticCacheService(vectorStore, 0.95, true)
        .store("where is my refund", "refund_status", "3-5 days")
        .as(StepVerifier::create)
        .verifyComplete();

    verify(vectorStore)
        .add(
            org.mockito.ArgumentMatchers.argThat(
                documents ->
                    documents.size() == 1
                        && documents.get(0).getText().equals("where is my refund")
                        && documents.get(0).getMetadata().get("answer").equals("3-5 days")
                        && documents.get(0).getMetadata().get("intentId").equals("refund_status")));
  }

  @Test
  void storeSkipsTheVectorStoreWhenDisabled() {
    new SemanticCacheService(vectorStore, 0.95, false)
        .store("where is my refund", "refund_status", "3-5 days")
        .as(StepVerifier::create)
        .verifyComplete();

    verify(vectorStore, never()).add(any());
  }
}
