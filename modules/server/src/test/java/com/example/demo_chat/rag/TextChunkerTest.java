package com.example.demo_chat.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextChunkerTest {

  private final TextChunker textChunker = new TextChunker();

  @Test
  void splitsTextOnWhitespaceKeepingATrailingSpaceOnEveryChunkButTheLast() {
    assertThat(textChunker.chunk("Refunds take 3-5 days."))
        .containsExactly("Refunds ", "take ", "3-5 ", "days.");
  }

  @Test
  void collapsesRepeatedWhitespaceBetweenWords() {
    assertThat(textChunker.chunk("Hello   world")).containsExactly("Hello ", "world");
  }

  @Test
  void returnsAOneElementListForASingleWord() {
    assertThat(textChunker.chunk("Hello")).containsExactly("Hello");
  }

  @Test
  void returnsAnEmptyListForBlankOrNullInput() {
    assertThat(textChunker.chunk("")).isEqualTo(List.of());
    assertThat(textChunker.chunk("   ")).isEqualTo(List.of());
    assertThat(textChunker.chunk(null)).isEqualTo(List.of());
  }
}
