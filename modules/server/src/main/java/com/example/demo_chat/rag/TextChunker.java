package com.example.demo_chat.rag;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

/** Splits an already-generated answer into small pieces for simulated SSE token streaming. */
@Component
public class TextChunker {

  /**
   * @param text the full text to split
   * @return {@code text} split on whitespace, preserving a single trailing space per chunk so the
   *     pieces can be concatenated back into the original text by the client
   */
  public List<String> chunk(String text) {
    if (text == null || text.isBlank()) {
      return List.of();
    }
    var words = text.trim().split("\\s+");
    return IntStream.range(0, words.length)
        .mapToObj(i -> i < words.length - 1 ? words[i] + " " : words[i])
        .toList();
  }
}
