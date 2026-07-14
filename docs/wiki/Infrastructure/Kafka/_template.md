<!-- Copy to Infrastructure/Kafka/<topic-name>.md, fill in, delete this comment, then link it from index.md -->

# Kafka Topic: <topic-name>

**Partitions:** _N_
**Replication factor:** _N_
**Key:** _what the message key is and why (determines partition assignment)_
**Retention:** _time/size policy_

## Schema

_Payload shape / Avro-Protobuf-JSON schema reference._

## Producers

- [[Feature Name]] — _what triggers a produce, roughly how often_

## Consumers

- [[Feature Name]] — _consumer group, what it does with the message_

## Notes

_Ordering guarantees, idempotency/dedup approach, DLQ, anything non-obvious._
