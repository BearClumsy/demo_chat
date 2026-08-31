output "bootstrap_brokers_tls" {
  description = "TLS bootstrap broker list. Maps to KAFKA_BOOTSTRAP_SERVERS."
  value       = aws_msk_cluster.this.bootstrap_brokers_tls
}

output "bootstrap_brokers_plaintext" {
  description = "Plaintext bootstrap broker list (in-VPC only)."
  value       = aws_msk_cluster.this.bootstrap_brokers
}

output "security_group_id" {
  description = "Security group attached to the brokers."
  value       = aws_security_group.msk.id
}
