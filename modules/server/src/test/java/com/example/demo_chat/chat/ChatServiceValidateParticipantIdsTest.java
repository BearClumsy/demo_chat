package com.example.demo_chat.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo_chat.user.User;
import com.example.demo_chat.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Exercises {@link ChatService#addParticipant} to cover the reactive {@code validateParticipantIds}
 * rewrite (Flux/Set semantics after the R2DBC migration) and the participant gate on the caller.
 */
class ChatServiceValidateParticipantIdsTest {

  @Mock private ChatHistoryRepository chatHistoryRepository;
  @Mock private UserRepository userRepository;

  private ChatService chatService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    chatService = new ChatService(chatHistoryRepository, userRepository);
  }

  @Test
  void addParticipantSucceedsWhenAllParticipantIdsAreValid() {
    var chatId = UUID.randomUUID();
    var currentUserId = UUID.randomUUID();
    var newParticipantId = UUID.randomUUID();
    var user = User.builder().id(newParticipantId).build();
    var existing =
        ChatHistory.builder().userId(chatId).participantIds(List.of(currentUserId)).build();
    var updated =
        existing.toBuilder().participantIds(List.of(currentUserId, newParticipantId)).build();

    when(userRepository.findAllById(List.of(newParticipantId))).thenReturn(Flux.just(user));
    when(chatHistoryRepository.findById(chatId)).thenReturn(Mono.just(existing));
    when(chatHistoryRepository.save(any(ChatHistory.class))).thenReturn(Mono.just(updated));

    chatService
        .addParticipant(chatId, currentUserId, newParticipantId)
        .as(StepVerifier::create)
        .expectNextMatches(result -> result.getParticipantIds().contains(newParticipantId))
        .verifyComplete();
  }

  @Test
  void addParticipantFailsWhenParticipantIdIsNotARealUser() {
    var chatId = UUID.randomUUID();
    var currentUserId = UUID.randomUUID();
    var unknownUserId = UUID.randomUUID();
    var existing =
        ChatHistory.builder().userId(chatId).participantIds(List.of(currentUserId)).build();

    when(chatHistoryRepository.findById(chatId)).thenReturn(Mono.just(existing));
    when(userRepository.findAllById(List.of(unknownUserId))).thenReturn(Flux.empty());

    chatService
        .addParticipant(chatId, currentUserId, unknownUserId)
        .as(StepVerifier::create)
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Participant ids must be valid"))
        .verify();

    verify(chatHistoryRepository, never()).save(any(ChatHistory.class));
  }

  /** The caller must already be in the chat, so a stranger can't add themselves into it. */
  @Test
  void addParticipantFailsWhenCallerIsNotAParticipant() {
    var chatId = UUID.randomUUID();
    var outsiderId = UUID.randomUUID();
    var existing =
        ChatHistory.builder().userId(chatId).participantIds(List.of(UUID.randomUUID())).build();

    when(chatHistoryRepository.findById(chatId)).thenReturn(Mono.just(existing));

    chatService
        .addParticipant(chatId, outsiderId, outsiderId)
        .as(StepVerifier::create)
        .expectError(AccessDeniedException.class)
        .verify();

    verify(userRepository, never()).findAllById(anyIterable());
    verify(chatHistoryRepository, never()).save(any(ChatHistory.class));
  }
}
