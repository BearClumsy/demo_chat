package com.example.demo_chat.rag;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Pushes every {@link IntentDefinition} into the Qdrant {@code support_kb} collection on startup.
 * Document ids are a name-based UUID derived from the intent id (Qdrant requires point ids to be an
 * unsigned integer or a UUID), so re-running this on every restart just upserts the same points
 * rather than duplicating them. The intent id itself is kept in the {@code topic} metadata, which
 * is what the pipeline reads back.
 *
 * <p>Staging/prod disable startup reindexing ({@code demo-chat.rag.reindex-on-startup=false}). The
 * Kubernetes bootstrap Job (see {@code infra/k8s/manifest-*.yaml}) instead starts the server image
 * with {@code --reindex-and-exit}: this runner seeds {@code support_kb} once and then stops the
 * application with exit code 0, so the Job completes instead of the pod staying up.
 */
@Component
public class KnowledgeBaseIndexer implements ApplicationRunner {

  private final IntentDefinitionRegistry registry;
  private final VectorStore vectorStore;
  private final ConfigurableApplicationContext applicationContext;
  private final boolean reindexOnStartup;

  public KnowledgeBaseIndexer(
      IntentDefinitionRegistry registry,
      VectorStore vectorStore,
      ConfigurableApplicationContext applicationContext,
      @Value("${demo-chat.rag.reindex-on-startup:true}") boolean reindexOnStartup) {
    this.registry = registry;
    this.vectorStore = vectorStore;
    this.applicationContext = applicationContext;
    this.reindexOnStartup = reindexOnStartup;
  }

  @Override
  public void run(ApplicationArguments args) {
    var reindexAndExit = args.containsOption("reindex-and-exit");
    if (!reindexOnStartup && !reindexAndExit) {
      return;
    }
    Mono.fromRunnable(this::reindex).subscribeOn(Schedulers.boundedElastic()).block();
    if (reindexAndExit) {
      System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
  }

  private void reindex() {
    var documents = registry.findAll().stream().map(this::toDocument).toList();
    vectorStore.add(documents);
  }

  private Document toDocument(IntentDefinition intent) {
    return Document.builder()
        .id(deterministicId(intent.intentId()))
        .text(intent.knowledgeSnippet())
        .metadata("topic", intent.intentId())
        .metadata("allowed", intent.allowed())
        .build();
  }

  /**
   * Maps an intent id to a stable name-based (v3) UUID. Qdrant point ids must be an unsigned
   * integer or a UUID, so the intent id can't be used verbatim; deriving it keeps re-indexing
   * idempotent.
   */
  private static String deterministicId(String intentId) {
    return UUID.nameUUIDFromBytes(intentId.getBytes(StandardCharsets.UTF_8)).toString();
  }
}
