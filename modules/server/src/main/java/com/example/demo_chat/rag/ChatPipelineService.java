package com.example.demo_chat.rag;

import com.example.demo_chat.chat.ChatHistory;
import com.example.demo_chat.chat.ChatHistoryRepository;
import com.example.demo_chat.chat.ChatMessage;
import com.example.demo_chat.chat.SendMessageResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Orchestrates the RAG pipeline for a single incoming chat message: normalize → retrieve → classify
 * → scope-check → slot-fill → generate. Persists the resulting {@link DialogueState} and appends
 * the user/assistant turn to the chat's {@link ChatHistory}.
 */
@Service
public class ChatPipelineService {

  /** Well-known sender id used for the bot's own replies in {@link ChatHistory#getMessages()}. */
  private static final UUID ASSISTANT_SENDER_ID = new UUID(0L, 0L);

  private static final String OUT_OF_SCOPE_REPLY =
      "I'm not able to help with that here. Let me connect you with a human agent.";

  private final IntentDefinitionRegistry registry;
  private final QueryNormalizationService normalizationService;
  private final KnowledgeRetrievalService retrievalService;
  private final IntentClassificationService classificationService;
  private final ScopeFilter scopeFilter;
  private final SlotFillingService slotFillingService;
  private final PromptBuilder promptBuilder;
  private final AnswerGenerationService answerGenerationService;
  private final ResponseValidator responseValidator;
  private final SemanticCacheService semanticCacheService;
  private final DialogueStateRepository dialogueStateRepository;
  private final ChatHistoryRepository chatHistoryRepository;
  private final TextChunker textChunker;
  private final long chunkDelayMillis;

  public ChatPipelineService(
      IntentDefinitionRegistry registry,
      QueryNormalizationService normalizationService,
      KnowledgeRetrievalService retrievalService,
      IntentClassificationService classificationService,
      ScopeFilter scopeFilter,
      SlotFillingService slotFillingService,
      PromptBuilder promptBuilder,
      AnswerGenerationService answerGenerationService,
      ResponseValidator responseValidator,
      SemanticCacheService semanticCacheService,
      DialogueStateRepository dialogueStateRepository,
      ChatHistoryRepository chatHistoryRepository,
      TextChunker textChunker,
      @Value("${demo-chat.streaming.chunk-delay-millis:40}") long chunkDelayMillis) {
    this.registry = registry;
    this.normalizationService = normalizationService;
    this.retrievalService = retrievalService;
    this.classificationService = classificationService;
    this.scopeFilter = scopeFilter;
    this.slotFillingService = slotFillingService;
    this.promptBuilder = promptBuilder;
    this.answerGenerationService = answerGenerationService;
    this.responseValidator = responseValidator;
    this.semanticCacheService = semanticCacheService;
    this.dialogueStateRepository = dialogueStateRepository;
    this.chatHistoryRepository = chatHistoryRepository;
    this.textChunker = textChunker;
    this.chunkDelayMillis = chunkDelayMillis;
  }

  /**
   * @param chatId the chat this message belongs to
   * @param userId the user sending the message
   * @param rawMessage the raw message text
   * @return the assistant's reply and the resulting dialogue status
   */
  public Mono<SendMessageResponse> handleMessage(UUID chatId, UUID userId, String rawMessage) {
    return dialogueStateRepository
        .findById(chatId)
        .defaultIfEmpty(DialogueState.newState(chatId))
        .flatMap(state -> continuePipeline(state, rawMessage))
        .flatMap(outcome -> persistAndReturn(chatId, userId, rawMessage, outcome));
  }

  /**
   * Same pipeline as {@link #handleMessage}, but streams the already guardrail-validated reply as
   * chunked SSE {@code token} events followed by one {@code done} event, instead of waiting for the
   * whole answer before responding.
   *
   * @param chatId the chat this message belongs to
   * @param userId the user sending the message
   * @param rawMessage the raw message text
   * @return a stream of {@code token} events followed by one {@code done} event carrying the
   *     resulting dialogue status
   */
  public Flux<ServerSentEvent<String>> handleMessageStream(
      UUID chatId, UUID userId, String rawMessage) {
    return dialogueStateRepository
        .findById(chatId)
        .defaultIfEmpty(DialogueState.newState(chatId))
        .flatMap(state -> continuePipeline(state, rawMessage))
        .flatMapMany(outcome -> streamOutcome(chatId, userId, rawMessage, outcome));
  }

  private Flux<ServerSentEvent<String>> streamOutcome(
      UUID chatId, UUID userId, String rawMessage, PipelineOutcome outcome) {
    var tokenEvents =
        Flux.fromIterable(textChunker.chunk(outcome.reply()))
            .delayElements(Duration.ofMillis(chunkDelayMillis))
            .map(chunk -> ServerSentEvent.builder(chunk).event("token").build());
    var doneEvent =
        persistOutcome(chatId, userId, rawMessage, outcome)
            .thenReturn(
                ServerSentEvent.builder(outcome.state().getStatus().name()).event("done").build());
    return tokenEvents.concatWith(doneEvent);
  }

  private Mono<PipelineOutcome> continuePipeline(DialogueState state, String rawMessage) {
    if (state.getStatus() == DialogueStatus.SLOT_FILLING && state.getCurrentIntentId() != null) {
      var intent = registry.findById(state.getCurrentIntentId());
      if (intent.isPresent()) {
        return continueSlotFilling(state, intent.get(), rawMessage);
      }
    }
    return startNewTurn(state, rawMessage);
  }

  private Mono<PipelineOutcome> continueSlotFilling(
      DialogueState state, IntentDefinition intent, String rawMessage) {
    var missingBefore = slotFillingService.missingSlots(intent, state.getSlots());
    if (missingBefore.isEmpty()) {
      return generateAnswer(state, intent);
    }
    var newSlots = new HashMap<>(state.getSlots());
    newSlots.put(missingBefore.get(0), rawMessage.trim());
    var updated = state.toBuilder().slots(newSlots).updatedAt(Instant.now()).build();

    var stillMissing = slotFillingService.missingSlots(intent, newSlots);
    if (!stillMissing.isEmpty()) {
      return answerGenerationService
          .generateClarifyingQuestion(intent, stillMissing.get(0))
          .map(
              question ->
                  new PipelineOutcome(
                      updated.toBuilder().status(DialogueStatus.SLOT_FILLING).build(), question));
    }
    return generateAnswer(updated, intent);
  }

  private Mono<PipelineOutcome> startNewTurn(DialogueState state, String rawMessage) {
    return normalizationService
        .normalize(rawMessage)
        .flatMap(
            normalizedQuery ->
                semanticCacheService
                    .lookup(normalizedQuery)
                    .map(cachedAnswer -> cacheHit(state, normalizedQuery, cachedAnswer))
                    .switchIfEmpty(
                        Mono.defer(
                            () ->
                                retrievalService
                                    .retrieve(normalizedQuery)
                                    .map(this::toCandidateIntents)
                                    .flatMap(
                                        candidates ->
                                            classifyAndRoute(
                                                state, normalizedQuery, candidates)))));
  }

  private PipelineOutcome cacheHit(
      DialogueState state, String normalizedQuery, String cachedAnswer) {
    var updated =
        state.toBuilder()
            .status(DialogueStatus.ANSWERED)
            .lastNormalizedQuery(normalizedQuery)
            .updatedAt(Instant.now())
            .build();
    return new PipelineOutcome(updated, cachedAnswer);
  }

  private List<IntentDefinition> toCandidateIntents(List<Document> documents) {
    return documents.stream()
        .map(document -> (String) document.getMetadata().get("topic"))
        .filter(Objects::nonNull)
        .map(registry::findById)
        .flatMap(Optional::stream)
        .toList();
  }

  private Mono<PipelineOutcome> classifyAndRoute(
      DialogueState state, String normalizedQuery, List<IntentDefinition> candidates) {
    if (candidates.isEmpty()) {
      return Mono.just(outOfScope(state));
    }
    return classificationService
        .classify(normalizedQuery, candidates)
        .map(classification -> scopeFilter.resolve(classification, candidates))
        .flatMap(
            matched -> {
              if (matched.isEmpty()) {
                return Mono.just(outOfScope(state));
              }
              var intent = matched.get();
              var started =
                  state.toBuilder()
                      .currentIntentId(intent.intentId())
                      .slots(Map.of())
                      .lastNormalizedQuery(normalizedQuery)
                      .updatedAt(Instant.now())
                      .build();
              var missing = slotFillingService.missingSlots(intent, started.getSlots());
              if (!missing.isEmpty()) {
                return answerGenerationService
                    .generateClarifyingQuestion(intent, missing.get(0))
                    .map(
                        question ->
                            new PipelineOutcome(
                                started.toBuilder().status(DialogueStatus.SLOT_FILLING).build(),
                                question));
              }
              return generateAnswer(started, intent);
            });
  }

  private Mono<PipelineOutcome> generateAnswer(DialogueState state, IntentDefinition intent) {
    var query = state.getLastNormalizedQuery() != null ? state.getLastNormalizedQuery() : "";
    var prompt = promptBuilder.build(intent, state.getSlots(), query);
    return answerGenerationService
        .generate(prompt)
        .flatMap(
            answer ->
                responseValidator
                    .validate(answer, intent)
                    .flatMap(
                        grounded -> {
                          if (!grounded) {
                            return Mono.just(
                                new PipelineOutcome(
                                    state.toBuilder().status(DialogueStatus.ESCALATED).build(),
                                    intent.escalationFallback()));
                          }
                          return semanticCacheService
                              .store(query, intent.intentId(), answer)
                              .thenReturn(
                                  new PipelineOutcome(
                                      state.toBuilder().status(DialogueStatus.ANSWERED).build(),
                                      answer));
                        }));
  }

  private PipelineOutcome outOfScope(DialogueState state) {
    var updated =
        state.toBuilder()
            .status(DialogueStatus.OUT_OF_SCOPE)
            .currentIntentId(null)
            .slots(Map.of())
            .updatedAt(Instant.now())
            .build();
    return new PipelineOutcome(updated, OUT_OF_SCOPE_REPLY);
  }

  private Mono<SendMessageResponse> persistAndReturn(
      UUID chatId, UUID userId, String rawMessage, PipelineOutcome outcome) {
    return persistOutcome(chatId, userId, rawMessage, outcome)
        .thenReturn(new SendMessageResponse(outcome.reply(), outcome.state().getStatus().name()));
  }

  private Mono<Void> persistOutcome(
      UUID chatId, UUID userId, String rawMessage, PipelineOutcome outcome) {
    return dialogueStateRepository
        .save(outcome.state())
        .then(appendMessages(chatId, userId, rawMessage, outcome.reply()))
        .then();
  }

  private Mono<ChatHistory> appendMessages(
      UUID chatId, UUID userId, String rawMessage, String reply) {
    return chatHistoryRepository
        .findById(chatId)
        .flatMap(
            chatHistory -> {
              var messages = new ArrayList<>(chatHistory.getMessages());
              var now = Instant.now();
              messages.add(
                  ChatMessage.builder().senderId(userId).content(rawMessage).sentAt(now).build());
              messages.add(
                  ChatMessage.builder()
                      .senderId(ASSISTANT_SENDER_ID)
                      .content(reply)
                      .sentAt(now)
                      .build());
              return chatHistoryRepository.save(chatHistory.toBuilder().messages(messages).build());
            });
  }

  private record PipelineOutcome(DialogueState state, String reply) {}
}
