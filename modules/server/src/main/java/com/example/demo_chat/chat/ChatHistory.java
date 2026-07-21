package com.example.demo_chat.chat;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Frozen;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("chat_history")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatHistory {

  @PrimaryKey("user_id")
  private UUID userId;

  @Column("participant_ids")
  private List<UUID> participantIds;

  @Column("title")
  private String title;

  @Frozen
  @Column("messages")
  private List<ChatMessage> messages;
}
