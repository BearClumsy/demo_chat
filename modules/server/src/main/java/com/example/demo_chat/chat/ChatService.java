package com.example.demo_chat.chat;

import com.example.demo_chat.rag.ChatPipelineService;
import com.example.demo_chat.user.User;
import com.example.demo_chat.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatHistoryRepository chatHistoryRepository;
  private final UserRepository userRepository;
  private final ChatPipelineService chatPipelineService;

  /**
   * @param currentUserId the id of the authenticated user starting the chat
   * @param participantIds the ids of the other users to invite into the chat; may be empty or
   *     {@code null} to start a chat with only the AI assistant
   * @param title the chat's title
   * @param message the chat's initial message
   * @return the new chat's id together with the assistant's reply to the opening message and the
   *     resulting dialogue status
   * @throws IllegalArgumentException if a non-empty {@code participantIds} includes {@code
   *     currentUserId} or any id that isn't a real user
   */
  public Mono<StartChatResponse> startChat(
      UUID currentUserId, List<UUID> participantIds, String title, MessageRequest message) {
    var others = participantIds == null ? List.<UUID>of() : participantIds;
    if (others.contains(currentUserId)) {
      return Mono.error(new IllegalArgumentException("Cannot start a chat with yourself"));
    }
    var validation = others.isEmpty() ? Mono.<Void>empty() : validateParticipantIds(others);
    return validation.then(
        Mono.defer(
            () -> {
              // The caller is always prepended, so the stored list is never an empty Cassandra
              // collection (which would read back as null and break getChatForParticipant).
              var allParticipantIds = new ArrayList<UUID>();
              allParticipantIds.add(currentUserId);
              allParticipantIds.addAll(others);
              var chatId = UUID.randomUUID();
              var chatHistory =
                  ChatHistory.builder()
                      .userId(chatId)
                      .participantIds(allParticipantIds)
                      .title(title)
                      .messages(List.of())
                      .build();
              return chatHistoryRepository
                  .save(chatHistory)
                  .then(
                      chatPipelineService.handleMessage(
                          chatId, message.userId(), message.message()))
                  .map(
                      response ->
                          new StartChatResponse(chatId, response.reply(), response.status()));
            }));
  }

  /**
   * @param chatId the chat to add the participant to
   * @param currentUserId the id of the authenticated user adding the participant
   * @param userId the id of the user to add as a participant
   * @return the updated chat, or empty if no chat has this id
   * @throws AccessDeniedException if {@code currentUserId} isn't already a participant in the chat
   * @throws IllegalArgumentException if {@code userId} isn't a real user
   */
  public Mono<ChatHistory> addParticipant(UUID chatId, UUID currentUserId, UUID userId) {
    return getChatForParticipant(chatId, currentUserId)
        .flatMap(chatHistory -> validateParticipantIds(List.of(userId)).thenReturn(chatHistory))
        .flatMap(
            chatHistory -> {
              if (chatHistory.getParticipantIds().contains(userId)) {
                return Mono.just(chatHistory);
              }
              var participantIds = new ArrayList<>(chatHistory.getParticipantIds());
              participantIds.add(userId);
              return chatHistoryRepository.save(
                  chatHistory.toBuilder().participantIds(participantIds).build());
            });
  }

  /**
   * @param chatId the chat to look up
   * @param userId the user requesting access
   * @return the chat, or empty if no chat has this id
   * @throws AccessDeniedException if {@code userId} isn't a participant in the chat
   */
  public Mono<ChatHistory> getChatForParticipant(UUID chatId, UUID userId) {
    return chatHistoryRepository
        .findById(chatId)
        .flatMap(
            chatHistory -> {
              if (!chatHistory.getParticipantIds().contains(userId)) {
                return Mono.error(new AccessDeniedException("userId must be a chat participant"));
              }
              return Mono.just(chatHistory);
            });
  }

  private Mono<Void> validateParticipantIds(List<UUID> participantIds) {
    return userRepository
        .findAllById(participantIds)
        .map(User::getId)
        .collect(Collectors.toSet())
        .flatMap(
            foundIds ->
                foundIds.containsAll(participantIds)
                    ? Mono.<Void>empty()
                    : Mono.<Void>error(
                        new IllegalArgumentException("Participant ids must be valid")));
  }
}
