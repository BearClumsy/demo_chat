package com.example.demo_chat.rag;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import reactor.test.StepVerifier;

class ResponseValidatorTest {

  private final ChatClient chatClient = mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);

  @Test
  void validateReturnsTrueWhenAnswerIsGrounded() {
    when(chatClient
            .prompt()
            .system(anyString())
            .user(anyString())
            .call()
            .entity(GroundednessCheck.class))
        .thenReturn(new GroundednessCheck(true, "matches the knowledge snippet"));

    new ResponseValidator(chatClient, true)
        .validate("Refunds take 3-5 business days.", intent())
        .as(StepVerifier::create)
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void validateReturnsFalseWhenAnswerIsNotGrounded() {
    when(chatClient
            .prompt()
            .system(anyString())
            .user(anyString())
            .call()
            .entity(GroundednessCheck.class))
        .thenReturn(new GroundednessCheck(false, "invents a refund amount not in the context"));

    new ResponseValidator(chatClient, true)
        .validate("Your refund of $42 has been sent.", intent())
        .as(StepVerifier::create)
        .expectNext(false)
        .verifyComplete();
  }

  @Test
  void validateSkipsTheLlmCallWhenDisabled() {
    new ResponseValidator(chatClient, false)
        .validate("anything", intent())
        .as(StepVerifier::create)
        .expectNext(true)
        .verifyComplete();
  }

  private IntentDefinition intent() {
    return new IntentDefinition(
        "refund_status",
        java.util.List.of("Where is my refund?"),
        java.util.List.of("order_id"),
        "Refunds are processed within 3-5 business days from the moment the request is confirmed.",
        "Answer only about refund status.",
        true,
        "For order {order_id}: the refund is processed within 3-5 days.",
        "If the status is unclear, please contact a human agent.");
  }
}
