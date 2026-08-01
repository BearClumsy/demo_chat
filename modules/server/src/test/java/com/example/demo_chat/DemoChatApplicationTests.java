package com.example.demo_chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datastax.oss.driver.api.core.CqlSession;
import com.example.demo_chat.rag.ChatPipelineService;
import com.example.demo_chat.rag.IntentDefinitionRegistry;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full-context wiring test. The three data stores run as containers so this passes on a clean
 * machine with nothing but Docker; only Bedrock is stubbed, because reaching it would need AWS
 * credentials that CI does not have. Anything that breaks the bean graph — a missing property, a
 * broken Flyway migration, a vector store that can't initialize its collection — fails here.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class DemoChatApplicationTests {

  private static final String KEYSPACE = "demo_chat";
  private static final String LOCAL_DATACENTER = "datacenter1";
  private static final int EMBEDDING_DIMENSIONS = 1024;

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static final GenericContainer<?> CASSANDRA =
      new GenericContainer<>("cassandra:5")
          .withExposedPorts(9042)
          .withEnv("CASSANDRA_CLUSTER_NAME", "demo_chat")
          // Same heap cap as the local docker-compose stack, for the same reason: the default
          // auto-sized heap gets OOM-killed on a small runner.
          .withEnv("MAX_HEAP_SIZE", "512M")
          .withEnv("HEAP_NEWSIZE", "128M")
          .waitingFor(
              Wait.forLogMessage(".*Starting listening for CQL clients.*", 1)
                  .withStartupTimeout(Duration.ofMinutes(5)));

  @Container
  static final GenericContainer<?> QDRANT =
      new GenericContainer<>("qdrant/qdrant:latest")
          .withExposedPorts(6333, 6334)
          .waitingFor(Wait.forHttp("/readyz").forPort(6333));

  @DynamicPropertySource
  static void containerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add(
        "spring.r2dbc.url",
        () ->
            "r2dbc:postgresql://%s:%d/%s?schema=demo_chat"
                .formatted(
                    POSTGRES.getHost(), POSTGRES.getFirstMappedPort(), POSTGRES.getDatabaseName()));
    registry.add("spring.r2dbc.username", POSTGRES::getUsername);
    registry.add("spring.r2dbc.password", POSTGRES::getPassword);

    registry.add("spring.cassandra.contact-points", CASSANDRA::getHost);
    registry.add("spring.cassandra.port", () -> CASSANDRA.getMappedPort(9042));

    registry.add("spring.ai.vectorstore.qdrant.host", QDRANT::getHost);
    registry.add("spring.ai.vectorstore.qdrant.port", () -> QDRANT.getMappedPort(6334));
  }

  /**
   * Spring Data connects to an existing keyspace; {@code schema-action=create-if-not-exists}
   * creates tables but not the keyspace itself, so it has to exist before the context starts. JUnit
   * runs this after the containers are up and before Spring loads the context.
   */
  @BeforeAll
  static void createKeyspace() {
    try (var session =
        CqlSession.builder()
            .addContactPoint(
                new InetSocketAddress(CASSANDRA.getHost(), CASSANDRA.getMappedPort(9042)))
            .withLocalDatacenter(LOCAL_DATACENTER)
            .build()) {
      session.execute(
          "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy','replication_factor':1}"
              .formatted(KEYSPACE));
    }
  }

  @TestConfiguration
  static class StubBedrockModels {

    @Bean
    EmbeddingModel embeddingModel() {
      var vector = new float[EMBEDDING_DIMENSIONS];
      Arrays.fill(vector, 0.1f);
      var embeddingModel = mock(EmbeddingModel.class);
      // Qdrant sizes its collection from dimensions() while the vector store bean initializes, so
      // this has to be stubbed here rather than inside a test method.
      when(embeddingModel.dimensions()).thenReturn(EMBEDDING_DIMENSIONS);
      when(embeddingModel.embed(anyString())).thenReturn(vector);
      when(embeddingModel.embed(any(Document.class))).thenReturn(vector);
      when(embeddingModel.call(any()))
          .thenReturn(new EmbeddingResponse(List.of(new Embedding(vector, 0))));
      return embeddingModel;
    }

    @Bean
    ChatModel chatModel() {
      // Nothing calls the model during startup — ChatClient.builder() only holds on to it — so an
      // unstubbed mock is enough to complete the bean graph.
      return mock(ChatModel.class);
    }
  }

  @Autowired private ChatPipelineService chatPipelineService;
  @Autowired private IntentDefinitionRegistry intentDefinitionRegistry;
  @Autowired private VectorStore vectorStore;

  @Test
  void contextLoads() {
    assertThat(chatPipelineService).isNotNull();
    assertThat(vectorStore).isNotNull();
  }

  @Test
  void intentDefinitionsAreLoadedFromTheKnowledgeBase() {
    assertThat(intentDefinitionRegistry.findAll()).isNotEmpty();
  }
}
