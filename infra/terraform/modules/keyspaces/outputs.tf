output "keyspace_name" {
  description = "Keyspace name. Maps to CASSANDRA_KEYSPACE."
  value       = aws_keyspaces_keyspace.this.name
}

output "keyspace_arn" {
  description = "ARN of the keyspace."
  value       = aws_keyspaces_keyspace.this.arn
}
