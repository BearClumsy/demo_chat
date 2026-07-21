package com.example.demo_chat.rag;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.demo_chat.chat.ChatHistory;
import com.example.demo_chat.chat.ChatHistoryRepository;
import com.example.demo_chat.chat.SendMessageResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Covers the output-side guardrail branch of {@link ChatPipelineService#generateAnswer} (a grounded
 * answer is returned as-is / {@code ANSWERED}; an ungrounded one is replaced by the intent's
 * escalation fallback / {@code ESCALATED}) and the semantic cache: a cache hit short-circuits
 * retrieval/classification/generation, and only a guardrail-passed answer is cached.
 */
class ChatPipelineServiceTest {

  private final IntentDefinitionRegistry registry = mock(IntentDefinitionRegistry.class);
  private final QueryNormalizationService normalizationService =
      mock(QueryNormalizationService.class);
  private final KnowledgeRetrievalService retrievalService = mock(KnowledgeRetrievalService.class);
  private final IntentClassificationService classificationService =
      mock(IntentClassificationService.class);
  private final ScopeFilter scopeFilter = mock(ScopeFilter.class);
  private final SlotFillingService slotFillingService = mock(SlotFillingService.class);
  private final PromptBuilder promptBuilder = mock(PromptBuilder.class);
  private final AnswerGenerationService answerGenerationService =
      mock(AnswerGenerationService.class);
  private final ResponseValidator responseValidator = mock(ResponseValidator.class);
  private final SemanticCacheService semanticCacheService = mock(SemanticCacheService.class);
  private final DialogueStateRepository dialogueStateRepository =
      mock(DialogueStateRepository.class);
  private final ChatHistoryRepository chatHistoryRepository = mock(ChatHistoryRepository.class);

  private ChatPipelineService chatPipelineService;

  private static final UUID CHAT_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();
  private static final String RAW_MESSAGE = "Where is my refund?";
  private static final String NORMALIZED_QUERY = "where is my refund";

  private IntentDefinition intent;

  @BeforeEach
  void setUp() {
    chatPipelineService =
        new ChatPipelineService(
            registry,
            normalizationService,
            retrievalService,
            classificationService,
            scopeFilter,
            slotFillingService,
            promptBuilder,
            answerGenerationService,
            responseValidator,
            semanticCacheService,
            dialogueStateRepository,
            chatHistoryRepository,
            new TextChunker(),
            0L);

    intent =
        new IntentDefinition(
            "refund_status",
            List.of("Where is my refund?"),
            List.of(),
            "Refunds are processed within 3-5 business days.",
            "Answer only about refund status.",
            true,
            "Refund answer template",
            "If the status is unclear, please contact a human agent.");

    Document document =
        Document.builder().text(NORMALIZED_QUERY).metadata("topic", "refund_status").build();

    when(dialogueStateRepository.findById(CHAT_ID)).thenReturn(Mono.empty());
    when(normalizationService.normalize(RAW_MESSAGE)).thenReturn(Mono.just(NORMALIZED_QUERY));
    when(semanticCacheService.lookup(NORMALIZED_QUERY)).thenReturn(Mono.empty());
    when(retrievalService.retrieve(NORMALIZED_QUERY)).thenReturn(Mono.just(List.of(document)));
    when(registry.findById("refund_status")).thenReturn(Optional.of(intent));
    when(classificationService.classify(eq(NORMALIZED_QUERY), any()))
        .thenReturn(Mono.just(new IntentClassification("refund_status", 0.95)));
    when(scopeFilter.resolve(any(), any())).thenReturn(Optional.of(intent));
    when(slotFillingService.missingSlots(eq(intent), any())).thenReturn(List.of());
    when(promptBuilder.build(eq(intent), any(), eq(NORMALIZED_QUERY)))
        .thenReturn(new AssembledPrompt("system", "user"));
    when(semanticCacheService.store(any(), any(), any())).thenReturn(Mono.empty());
    when(dialogueStateRepository.save(any(DialogueState.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    when(chatHistoryRepository.findById(CHAT_ID))
        .thenReturn(
            Mono.just(
                ChatHistory.builder()
                    .userId(CHAT_ID)
                    .participantIds(List.of(USER_ID))
                    .messages(List.of())
                    .build()));
    when(chatHistoryRepository.save(any(ChatHistory.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
  }

  @Test
  void groundedAnswerIsReturnedAsAnsweredAndCached() {
    String generatedAnswer = "Refunds take 3-5 business days.";
    when(answerGenerationService.generate(any())).thenReturn(Mono.just(generatedAnswer));
    when(responseValidator.validate(generatedAnswer, intent)).thenReturn(Mono.just(true));

    chatPipelineService
        .handleMessage(CHAT_ID, USER_ID, RAW_MESSAGE)
        .as(StepVerifier::create)
        .expectNext(new SendMessageResponse(generatedAnswer, "ANSWERED"))
        .verifyComplete();

    verify(semanticCacheService).store(NORMALIZED_QUERY, "refund_status", generatedAnswer);
  }

  @Test
  void ungroundedAnswerIsReplacedByEscalationFallbackAndNeverCached() {
    String hallucinatedAnswer = "Your refund of $42 has already been sent.";
    when(answerGenerationService.generate(any())).thenReturn(Mono.just(hallucinatedAnswer));
    when(responseValidator.validate(hallucinatedAnswer, intent)).thenReturn(Mono.just(false));

    chatPipelineService
        .handleMessage(CHAT_ID, USER_ID, RAW_MESSAGE)
        .as(StepVerifier::create)
        .expectNext(
            new SendMessageResponse(
                "If the status is unclear, please contact a human agent.", "ESCALATED"))
        .verifyComplete();

    verify(semanticCacheService, never()).store(any(), any(), any());
  }

  @Test
  void cacheHitSkipsRetrievalClassificationAndGeneration() {
    String cachedAnswer = "Refunds take 3-5 business days.";
    when(semanticCacheService.lookup(NORMALIZED_QUERY)).thenReturn(Mono.just(cachedAnswer));

    chatPipelineService
        .handleMessage(CHAT_ID, USER_ID, RAW_MESSAGE)
        .as(StepVerifier::create)
        .expectNext(new SendMessageResponse(cachedAnswer, "ANSWERED"))
        .verifyComplete();

    verifyNoInteractions(retrievalService, classificationService, answerGenerationService);
  }

  @Test
  void handleMessageStreamEmitsTokenChunksThenADoneEvent() {
    String generatedAnswer = "Refunds take 3-5 days.";
    when(answerGenerationService.generate(any())).thenReturn(Mono.just(generatedAnswer));
    when(responseValidator.validate(generatedAnswer, intent)).thenReturn(Mono.just(true));

    chatPipelineService
        .handleMessageStream(CHAT_ID, USER_ID, RAW_MESSAGE)
        .as(StepVerifier::create)
        .expectNextMatches(tokenEvent("Refunds "))
        .expectNextMatches(tokenEvent("take "))
        .expectNextMatches(tokenEvent("3-5 "))
        .expectNextMatches(tokenEvent("days."))
        .expectNextMatches(event -> "done".equals(event.event()) && "ANSWERED".equals(event.data()))
        .verifyComplete();
  }

  private static Predicate<ServerSentEvent<String>> tokenEvent(String data) {
    return event -> "token".equals(event.event()) && data.equals(event.data());
  }
}
