package com.example.demo_chat.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo_chat.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Exercises {@link ChatService#startChat}, in particular the assistant-only case where no
 * participant ids are supplied.
 */
class ChatServiceStartChatTest {

  @Mock private ChatHistoryRepository chatHistoryRepository;
  @Mock private UserRepository userRepository;

  private ChatService chatService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    chatService = new ChatService(chatHistoryRepository, userRepository);
  }

  private static MessageRequest message(UUID userId) {
    return new MessageRequest(userId, "Where is my refund?", Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void startChatWithNoParticipantsPersistsOnlyTheCaller() {
    var callerId = UUID.randomUUID();
    when(chatHistoryRepository.save(any(ChatHistory.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

    chatService
        .startChat(callerId, List.of(), "Refund help", message(callerId))
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();

    var saved = ArgumentCaptor.forClass(ChatHistory.class);
    verify(chatHistoryRepository).save(saved.capture());
    assertEquals(List.of(callerId), saved.getValue().getParticipantIds());
    verify(userRepository, never()).findAllById(anyIterable());
  }

  @Test
  void startChatWithUnknownParticipantStillFails() {
    var callerId = UUID.randomUUID();
    var unknownId = UUID.randomUUID();
    when(userRepository.findAllById(List.of(unknownId))).thenReturn(Flux.empty());

    chatService
        .startChat(callerId, List.of(unknownId), "Nope", message(callerId))
        .as(StepVerifier::create)
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Participant ids must be valid"))
        .verify();

    verify(chatHistoryRepository, never()).save(any(ChatHistory.class));
  }

  @Test
  void startChatWithCallerAmongParticipantsFails() {
    var callerId = UUID.randomUUID();

    chatService
        .startChat(callerId, List.of(callerId), "Self", message(callerId))
        .as(StepVerifier::create)
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Cannot start a chat with yourself"))
        .verify();

    verify(userRepository, never()).findAllById(anyIterable());
    verify(chatHistoryRepository, never()).save(any(ChatHistory.class));
  }
}
