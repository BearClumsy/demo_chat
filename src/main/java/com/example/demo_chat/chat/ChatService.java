package com.example.demo_chat.chat;

import com.example.demo_chat.user.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
   * @param userId the id of the user to add as a participant
   * @return the updated chat, or empty if no chat has this id
   * @throws IllegalArgumentException if {@code userId} isn't a real user
   */
  public Mono<ChatHistory> addParticipant(UUID chatId, UUID userId) {
    return validateParticipantIds(List.of(userId))
        .then(chatHistoryRepository.findById(chatId))
        .flatMap(
            chatHistory -> {
              if (chatHistory.getParticipantIds().contains(userId)) {
                return Mono.just(chatHistory);
              }
              List<UUID> participantIds = new ArrayList<>(chatHistory.getParticipantIds());
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
    return Mono.fromCallable(() -> userRepository.findAllById(participantIds))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            users -> {
              Set<UUID> foundIds = new HashSet<>();
              users.forEach(user -> foundIds.add(user.getId()));
              if (!foundIds.containsAll(participantIds)) {
                return Mono.<Void>error(
                    new IllegalArgumentException("Participant ids must be valid"));
              }
              return Mono.<Void>empty();
            });
  }
}
