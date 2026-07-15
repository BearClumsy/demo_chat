package com.example.demo_chat.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Output-side guardrail: a post-generation groundedness check verifying that a generated answer
 * doesn't invent facts beyond the intent's {@code knowledgeSnippet} (see {@code
 * docs/wiki/Plan/prompt-engineering.md}).
 */
@Service
public class ResponseValidator {

  private static final String SYSTEM_PROMPT =
      """
      You are a groundedness checker for a customer support bot's answers. Given a CONTEXT (the \
      only facts the bot is allowed to use) and an ANSWER, decide whether the ANSWER is fully \
      grounded in the CONTEXT, with no invented facts. Respond with your verdict and a one-sentence \
      reason.""";

  private final ChatClient chatClient;
  private final boolean enabled;

  public ResponseValidator(
      ChatClient chatClient, @Value("${demo-chat.guardrail.enabled:true}") boolean enabled) {
    this.chatClient = chatClient;
    this.enabled = enabled;
  }

  /**
   * @param answer the generated answer to check
   * @param intent the intent whose {@code knowledgeSnippet} the answer must be grounded in
   * @return {@code true} if the answer is grounded (or the guardrail is disabled)
   */
  public Mono<Boolean> validate(String answer, IntentDefinition intent) {
    if (!enabled) {
      return Mono.just(true);
    }
    String userPrompt =
        """
        CONTEXT:
        %s

        ANSWER:
        %s"""
            .formatted(intent.knowledgeSnippet(), answer);
    return Mono.fromCallable(
            () ->
                chatClient
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(GroundednessCheck.class))
        .subscribeOn(Schedulers.boundedElastic())
        .map(GroundednessCheck::grounded);
  }
}
