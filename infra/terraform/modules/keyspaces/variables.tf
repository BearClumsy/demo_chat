variable "keyspace_name" {
  description = "Amazon Keyspaces keyspace name. Maps to CASSANDRA_KEYSPACE."
  type        = string
  default     = "demo_chat"
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
