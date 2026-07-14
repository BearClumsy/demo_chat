package com.example.demo_chat.chat;

import com.example.demo_chat.rag.ChatPipelineService;
import com.example.demo_chat.user.UserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;
  private final ChatPipelineService chatPipelineService;

  /**
   * @param principal the authenticated user starting the chat
   * @param request the chat's participants, title, and initial message
   * @return the new chat's id
   * @throws AccessDeniedException if {@code request.currentUserId()} doesn't match the
   *     authenticated user
   */
  @PostMapping
  public Mono<ResponseEntity<UUID>> startChat(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody StartChatRequest request) {
    if (!principal.getId().equals(request.currentUserId())) {
      throw new AccessDeniedException("currentUserId must match the authenticated user");
    }
    return chatService
        .startChat(principal.getId(), request.participantIds(), request.title(), request.message())
        .map(ResponseEntity::ok);
  }

  /**
   * @param chatId the chat to add the participant to
   * @param request the participant's user id
   * @return 204 if the participant was added, or 404 if no chat has this id
   */
  @PostMapping("/{chatId}/participants")
  public Mono<ResponseEntity<Void>> addParticipant(
      @PathVariable UUID chatId, @Valid @RequestBody ParticipantRequest request) {
    return chatService
        .addParticipant(chatId, request.userId())
        .map(chatHistory -> ResponseEntity.noContent().<Void>build())
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  /**
   * @param principal the authenticated user sending the message
   * @param chatId the chat to send the message into
   * @param request the message text
   * @return the RAG pipeline's reply and resulting dialogue status, or 404 if no chat has this id
   * @throws AccessDeniedException if {@code principal} isn't a participant in the chat
   */
  @PostMapping("/{chatId}/messages")
  public Mono<ResponseEntity<SendMessageResponse>> sendMessage(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID chatId,
      @Valid @RequestBody SendMessageRequest request) {
    return chatService
        .getChatForParticipant(chatId, principal.getId())
        .flatMap(
            chatHistory ->
                chatPipelineService.handleMessage(chatId, principal.getId(), request.message()))
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }
}
