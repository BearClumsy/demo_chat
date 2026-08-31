resource "aws_keyspaces_keyspace" "this" {
  name = var.keyspace_name

  tags = var.tags
}

# NOTE: table schemas are the skeleton's weakest point. The source of truth for the Cassandra
# schema is the JPA entities under com.example.demo_chat.chat / com.example.demo_chat.rag and the
# hand-written CQL in modules/server/src/main/resources/db/cassandra/*.cql. Only the simplest table
# is modelled here; `chat_history` and `dialogue_state` additionally use the `ChatMessage` UDT and
# a wider column set. TODO: port the full definitions (and an aws_keyspaces_type for the UDT)
# before a real apply.
resource "aws_keyspaces_table" "open_chats_by_bucket" {
  keyspace_name = aws_keyspaces_keyspace.this.name
  table_name    = "open_chats_by_bucket"

  schema_definition {
    column {
      name = "bucket"
      type = "text"
    }
    column {
      name = "chat_id"
      type = "uuid"
    }
    column {
      name = "last_activity_at"
      type = "timestamp"
    }

    partition_key {
      name = "bucket"
    }

    clustering_key {
      name     = "chat_id"
      order_by = "ASC"
    }
  }

  point_in_time_recovery {
    status = "ENABLED"
  }

  tags = var.tags
}
