package com.example.demo_chat.chat;

import java.util.UUID;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

/** Spring Data Cassandra repository for {@link ChatHistory}. */
public interface ChatHistoryRepository extends ReactiveCassandraRepository<ChatHistory, UUID> {}
