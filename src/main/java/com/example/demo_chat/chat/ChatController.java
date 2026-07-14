package com.example.demo_chat.chat;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @PostMapping
  public Mono<ResponseEntity<UUID>> createNewChat() {
    return chatService.startChat().map(ResponseEntity::ok);
  }
}
