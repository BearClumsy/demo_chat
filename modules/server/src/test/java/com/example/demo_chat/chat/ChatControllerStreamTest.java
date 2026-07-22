package com.example.demo_chat.chat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import com.example.demo_chat.rag.ChatPipelineService;
import com.example.demo_chat.user.User;
import com.example.demo_chat.user.UserPrincipal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.web.reactive.result.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Slice test for the SSE streaming endpoint, bound directly to a {@link ChatController} instance
 * (not {@code @WebFluxTest}) to avoid interaction between Spring Security's reactive test
 * infrastructure and the app's real {@code SecurityConfig}: verifies the {@code text/event-stream}
 * content type, the {@code token...done} event sequence, and the 404 path when the chat doesn't
 * exist / isn't accessible to the caller.
 */
class ChatControllerStreamTest {

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

  private static final UUID CHAT_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();

  @Test
  void streamsTokenEventsFollowedByADoneEvent() {
    when(chatService.getChatForParticipant(CHAT_ID, USER_ID))
        .thenReturn(Mono.just(ChatHistory.builder().userId(CHAT_ID).build()));
    when(chatPipelineService.handleMessageStream(CHAT_ID, USER_ID, "Where is my refund?"))
        .thenReturn(
            Flux.just(
                ServerSentEvent.builder("Refunds ").event("token").<String>build(),
                ServerSentEvent.builder("take ").event("token").<String>build(),
                ServerSentEvent.builder("3-5 days.").event("token").<String>build(),
                ServerSentEvent.builder("ANSWERED").event("done").<String>build()));

    webTestClient
        .mutateWith(mockUser(principal()))
        .post()
        .uri("/api/chats/{chatId}/messages/stream", CHAT_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new SendMessageRequest("Where is my refund?"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
        .returnResult(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
        .getResponseBody()
        .as(StepVerifier::create)
        .expectNextMatches(
            event -> "token".equals(event.event()) && "Refunds ".equals(event.data()))
        .expectNextMatches(event -> "token".equals(event.event()) && "take ".equals(event.data()))
        .expectNextMatches(
            event -> "token".equals(event.event()) && "3-5 days.".equals(event.data()))
        .expectNextMatches(event -> "done".equals(event.event()) && "ANSWERED".equals(event.data()))
        .verifyComplete();
  }

  @Test
  void returnsNotFoundWhenTheChatDoesNotExistOrIsNotAccessible() {
    when(chatService.getChatForParticipant(CHAT_ID, USER_ID)).thenReturn(Mono.empty());

    webTestClient
        .mutateWith(mockUser(principal()))
        .post()
        .uri("/api/chats/{chatId}/messages/stream", CHAT_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new SendMessageRequest("Where is my refund?"))
        .exchange()
        .expectStatus()
        .isNotFound();
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
