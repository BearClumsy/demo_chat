package com.example.demo_chat.chat;

import static org.mockito.ArgumentMatchers.any;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Exercises {@link ChatService#addParticipant} to cover the reactive {@code validateParticipantIds}
 * rewrite (Flux/Set semantics after the R2DBC migration).
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
    var newParticipantId = UUID.randomUUID();
    var user = User.builder().id(newParticipantId).build();
    var existing =
        ChatHistory.builder().userId(chatId).participantIds(List.of(UUID.randomUUID())).build();
    var updated =
        existing.toBuilder()
            .participantIds(List.of(existing.getParticipantIds().get(0), newParticipantId))
            .build();

    when(userRepository.findAllById(List.of(newParticipantId))).thenReturn(Flux.just(user));
    when(chatHistoryRepository.findById(chatId)).thenReturn(Mono.just(existing));
    when(chatHistoryRepository.save(any(ChatHistory.class))).thenReturn(Mono.just(updated));

    chatService
        .addParticipant(chatId, newParticipantId)
        .as(StepVerifier::create)
        .expectNextMatches(result -> result.getParticipantIds().contains(newParticipantId))
        .verifyComplete();
  }

  @Test
  void addParticipantFailsWhenParticipantIdIsNotARealUser() {
    var chatId = UUID.randomUUID();
    var unknownUserId = UUID.randomUUID();

    when(userRepository.findAllById(List.of(unknownUserId))).thenReturn(Flux.empty());
    // Constructed eagerly as the .then(Mono) argument, but never subscribed since
    // validateParticipantIds errors first - stub so the construction doesn't NPE.
    when(chatHistoryRepository.findById(chatId)).thenReturn(Mono.empty());

    chatService
        .addParticipant(chatId, unknownUserId)
        .as(StepVerifier::create)
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Participant ids must be valid"))
        .verify();

    verify(chatHistoryRepository, never()).save(any(ChatHistory.class));
  }
}
