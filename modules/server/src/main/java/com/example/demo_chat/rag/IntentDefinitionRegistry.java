package com.example.demo_chat.rag;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Loads every {@code knowledge-base/intents/*.json} file at startup into an in-memory map. This is
 * the single source of truth used both by {@link KnowledgeBaseIndexer} (what gets pushed into
 * Qdrant) and by the RAG pipeline (looking up the full {@link IntentDefinition} once an intent has
 * been classified).
 */
@Component
public class IntentDefinitionRegistry {

  private static final String LOCATION_PATTERN = "classpath*:knowledge-base/intents/*.json";

  private final ObjectMapper objectMapper;
  private Map<String, IntentDefinition> intentsById = Map.of();

  public IntentDefinitionRegistry(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  void load() {
    var resolver = new PathMatchingResourcePatternResolver();
    try {
      Resource[] resources = resolver.getResources(LOCATION_PATTERN);
      intentsById =
          List.of(resources).stream()
              .map(this::readIntent)
              .collect(Collectors.toMap(IntentDefinition::intentId, intent -> intent));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load knowledge-base intents", e);
    }
  }

  private IntentDefinition readIntent(Resource resource) {
    try (var in = resource.getInputStream()) {
      return objectMapper.readValue(in, IntentDefinition.class);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read intent definition: " + resource, e);
    }
  }

  /**
   * @param intentId the intent id to look up
   * @return the matching intent definition, or empty if none is registered with this id
   */
  public Optional<IntentDefinition> findById(String intentId) {
    return Optional.ofNullable(intentsById.get(intentId));
  }

  /**
   * @return every registered intent definition
   */
  public Collection<IntentDefinition> findAll() {
    return intentsById.values();
  }
}
