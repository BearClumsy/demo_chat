package com.example.demo_chat.rag;

import com.example.demo_chat.chat.ChatHistory;
import com.example.demo_chat.chat.ChatHistoryRepository;
import com.example.demo_chat.chat.ChatMessage;
import com.example.demo_chat.chat.SendMessageResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
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
  private final DialogueStateRepository dialogueStateRepository;
  private final ChatHistoryRepository chatHistoryRepository;

  public ChatPipelineService(
      IntentDefinitionRegistry registry,
      QueryNormalizationService normalizationService,
      KnowledgeRetrievalService retrievalService,
      IntentClassificationService classificationService,
      ScopeFilter scopeFilter,
      SlotFillingService slotFillingService,
      PromptBuilder promptBuilder,
      AnswerGenerationService answerGenerationService,
      DialogueStateRepository dialogueStateRepository,
      ChatHistoryRepository chatHistoryRepository) {
    this.registry = registry;
    this.normalizationService = normalizationService;
    this.retrievalService = retrievalService;
    this.classificationService = classificationService;
    this.scopeFilter = scopeFilter;
    this.slotFillingService = slotFillingService;
    this.promptBuilder = promptBuilder;
    this.answerGenerationService = answerGenerationService;
    this.dialogueStateRepository = dialogueStateRepository;
    this.chatHistoryRepository = chatHistoryRepository;
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

  private Mono<PipelineOutcome> continuePipeline(DialogueState state, String rawMessage) {
    if (state.getStatus() == DialogueStatus.SLOT_FILLING && state.getCurrentIntentId() != null) {
      Optional<IntentDefinition> intent = registry.findById(state.getCurrentIntentId());
      if (intent.isPresent()) {
        return continueSlotFilling(state, intent.get(), rawMessage);
      }
    }
    return startNewTurn(state, rawMessage);
  }

  private Mono<PipelineOutcome> continueSlotFilling(
      DialogueState state, IntentDefinition intent, String rawMessage) {
    List<String> missingBefore = slotFillingService.missingSlots(intent, state.getSlots());
    if (missingBefore.isEmpty()) {
      return generateAnswer(state, intent);
    }
    Map<String, String> newSlots = new HashMap<>(state.getSlots());
    newSlots.put(missingBefore.get(0), rawMessage.trim());
    DialogueState updated = state.toBuilder().slots(newSlots).updatedAt(Instant.now()).build();

    List<String> stillMissing = slotFillingService.missingSlots(intent, newSlots);
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
                retrievalService
                    .retrieve(normalizedQuery)
                    .map(this::toCandidateIntents)
                    .flatMap(candidates -> classifyAndRoute(state, normalizedQuery, candidates)));
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
              IntentDefinition intent = matched.get();
              DialogueState started =
                  state.toBuilder()
                      .currentIntentId(intent.intentId())
                      .slots(Map.of())
                      .lastNormalizedQuery(normalizedQuery)
                      .updatedAt(Instant.now())
                      .build();
              List<String> missing = slotFillingService.missingSlots(intent, started.getSlots());
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
    String query = state.getLastNormalizedQuery() != null ? state.getLastNormalizedQuery() : "";
    AssembledPrompt prompt = promptBuilder.build(intent, state.getSlots(), query);
    return answerGenerationService
        .generate(prompt)
        .map(
            answer ->
                new PipelineOutcome(
                    state.toBuilder().status(DialogueStatus.ANSWERED).build(), answer));
  }

  private PipelineOutcome outOfScope(DialogueState state) {
    DialogueState updated =
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
    return dialogueStateRepository
        .save(outcome.state())
        .then(appendMessages(chatId, userId, rawMessage, outcome.reply()))
        .thenReturn(new SendMessageResponse(outcome.reply(), outcome.state().getStatus().name()));
  }

  private Mono<ChatHistory> appendMessages(
      UUID chatId, UUID userId, String rawMessage, String reply) {
    return chatHistoryRepository
        .findById(chatId)
        .flatMap(
            chatHistory -> {
              List<ChatMessage> messages = new ArrayList<>(chatHistory.getMessages());
              Instant now = Instant.now();
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
