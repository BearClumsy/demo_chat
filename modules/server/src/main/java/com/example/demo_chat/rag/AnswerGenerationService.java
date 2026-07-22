package com.example.demo_chat.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Stage 7 of the RAG pipeline: generates the final answer, or — while a required slot is still
 * missing — a topic-constrained clarifying question.
 */
@Service
public class AnswerGenerationService {

  private static final String CLARIFYING_QUESTION_SYSTEM_TEMPLATE =
      """
      You are a support assistant helping with the "%s" topic: %s. The user hasn't yet provided \
      "%s", which is required before you can answer. Ask a single, short, friendly question \
      requesting exactly that information. Do not answer the original question yet and do not \
      discuss any other topic.""";

  private final ChatClient chatClient;

  public AnswerGenerationService(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  /**
   * @param prompt the assembled SYSTEM/CONTEXT/USER STATE/USER MESSAGE prompt
   * @return the generated answer
   */
  public Mono<String> generate(AssembledPrompt prompt) {
    return Mono.fromCallable(
            () -> chatClient.prompt().system(prompt.system()).user(prompt.user()).call().content())
        .subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * @param intent the confirmed, in-scope intent
   * @param missingSlot the next slot that still needs to be collected
   * @return a topic-constrained clarifying question
   */
  public Mono<String> generateClarifyingQuestion(IntentDefinition intent, String missingSlot) {
    var system =
        CLARIFYING_QUESTION_SYSTEM_TEMPLATE.formatted(
            intent.intentId(), intent.knowledgeSnippet(), missingSlot);
    return Mono.fromCallable(
            () ->
                chatClient
                    .prompt()
                    .system(system)
                    .user("Ask for the missing information now.")
                    .call()
                    .content())
        .subscribeOn(Schedulers.boundedElastic());
  }
}
