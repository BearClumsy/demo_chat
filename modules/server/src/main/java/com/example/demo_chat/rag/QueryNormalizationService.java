package com.example.demo_chat.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Stage 1 of the RAG pipeline: rewrites a raw user message into a clean support query. */
@Service
public class QueryNormalizationService {

  private static final String SYSTEM_PROMPT =
      """
      You rewrite raw customer support messages into a short, clean query that captures the \
      user's intent. Strip greetings, filler words, and emotional language. Return only the \
      rewritten query, with no explanation or quotation marks.""";

  private final ChatClient chatClient;

  public QueryNormalizationService(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  /**
   * @param rawMessage the user's raw message
   * @return the normalized query, or {@code rawMessage} itself if the model returns nothing
   */
  public Mono<String> normalize(String rawMessage) {
    return Mono.fromCallable(
            () -> chatClient.prompt().system(SYSTEM_PROMPT).user(rawMessage).call().content())
        .subscribeOn(Schedulers.boundedElastic())
        .map(String::trim)
        .defaultIfEmpty(rawMessage);
  }
}
