package com.example.demo_chat.chat;

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

  /**
   * @param currentUserId the id of the authenticated user starting the chat
   * @param participantIds the ids of the other users to invite into the chat
   * @param title the chat's title
   * @param message the chat's initial message
   * @return the new chat's id
   * @throws IllegalArgumentException if {@code participantIds} includes {@code currentUserId} or
   *     any id that isn't a real user
   */
  public Mono<UUID> startChat(
      UUID currentUserId, List<UUID> participantIds, String title, MessageRequest message) {
    if (participantIds.contains(currentUserId)) {
      return Mono.error(new IllegalArgumentException("Cannot start a chat with yourself"));
    }
    return validateParticipantIds(participantIds)
        .then(
            Mono.defer(
                () -> {
                  var allParticipantIds = new ArrayList<UUID>();
                  allParticipantIds.add(currentUserId);
                  allParticipantIds.addAll(participantIds);
                  var chatMessage =
                      ChatMessage.builder()
                          .senderId(message.userId())
                          .content(message.message())
                          .sentAt(message.datetime())
                          .build();
                  var chatHistory =
                      ChatHistory.builder()
                          .userId(UUID.randomUUID())
                          .participantIds(allParticipantIds)
                          .title(title)
                          .messages(List.of(chatMessage))
                          .build();
                  return chatHistoryRepository.save(chatHistory).map(ChatHistory::getUserId);
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
