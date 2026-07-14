package com.example.demo_chat.rag;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * A chat's RAG pipeline working state: which intent is currently being pursued, which slots have
 * been collected, and the dialogue status. Keyed by the same id as {@code ChatHistory.userId} — one
 * chat, one dialogue state. The chat's message log itself lives in {@code ChatHistory}, so there's
 * no duplicated history field here.
 */
@Table("dialogue_state")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DialogueState {

  @PrimaryKey("chat_id")
  private UUID chatId;

  @Column("status")
  private DialogueStatus status;

  @Column("current_intent_id")
  private String currentIntentId;

  @Column("slots")
  private Map<String, String> slots;

  @Column("last_normalized_query")
  private String lastNormalizedQuery;

  @Column("updated_at")
  private Instant updatedAt;

  /**
   * @param chatId the chat this dialogue state belongs to
   * @return a fresh dialogue state with no intent or slots collected yet
   */
  public static DialogueState newState(UUID chatId) {
    return DialogueState.builder()
        .chatId(chatId)
        .status(DialogueStatus.NEW)
        .slots(Map.of())
        .updatedAt(Instant.now())
        .build();
  }
}
