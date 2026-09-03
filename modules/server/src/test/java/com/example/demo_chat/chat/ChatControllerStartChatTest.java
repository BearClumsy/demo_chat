package com.example.demo_chat.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import com.example.demo_chat.rag.ChatPipelineService;
import com.example.demo_chat.user.User;
import com.example.demo_chat.user.UserPrincipal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.MediaType;
import org.springframework.security.web.reactive.result.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * Slice test for {@code POST /api/chats}, bound directly to a {@link ChatController} instance (same
 * harness rationale as {@link ChatControllerStreamTest}): verifies a chat can be started with the
 * {@code participantIds} field omitted entirely, i.e. a chat with only the AI assistant.
 */
class ChatControllerStartChatTest {

  private final ChatService chatService = mock(ChatService.class);
  private final ChatPipelineService chatPipelineService = mock(ChatPipelineService.class);

  private final WebTestClient webTestClient =
      WebTestClient.bindToController(new ChatController(chatService, chatPipelineService))
          .argumentResolvers(
              resolvers ->
                  resolvers.addCustomResolver(
                      new AuthenticationPrincipalArgumentResolver(
                          ReactiveAdapterRegistry.getSharedInstance())))
          .apply(springSecurity())
          .build();

  private static final UUID USER_ID = UUID.randomUUID();

  @Test
  void startsAChatWhenParticipantIdsIsAbsent() {
    var chatId = UUID.randomUUID();
    when(chatService.startChat(eq(USER_ID), eq(List.of()), eq("Refund help"), any()))
        .thenReturn(Mono.just(chatId));

    webTestClient
        .mutateWith(mockUser(principal()))
        .post()
        .uri("/api/chats")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"currentUserId":"%s","title":"Refund help",\
            "message":{"userId":"%s","message":"Hi","datetime":"2026-01-01T00:00:00Z"}}"""
                .formatted(USER_ID, USER_ID))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UUID.class)
        .isEqualTo(chatId);
  }

  private static UserPrincipal principal() {
    var user =
        User.builder()
            .id(USER_ID)
            .login("jane")
            .password("hashed")
            .email("jane@example.com")
            .build();
    return new UserPrincipal(user);
  }
}
