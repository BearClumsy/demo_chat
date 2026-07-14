package com.example.demo_chat.chat;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatHistoryRepository chatHistoryRepository;

  public Mono<UUID> startChat() {
    ChatHistory chatHistory =
        ChatHistory.builder()
            .userId(UUID.randomUUID())
            .participantIds(List.of())
            .messages(List.of())
            .build();
    return chatHistoryRepository.save(chatHistory).map(ChatHistory::getUserId);
  }
}
