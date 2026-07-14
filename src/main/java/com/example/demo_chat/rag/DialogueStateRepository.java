package com.example.demo_chat.rag;

import java.util.UUID;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

/** Spring Data Cassandra repository for {@link DialogueState}. */
public interface DialogueStateRepository extends ReactiveCassandraRepository<DialogueState, UUID> {}
