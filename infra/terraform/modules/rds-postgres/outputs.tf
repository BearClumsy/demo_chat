output "db_host" {
  description = "Connection endpoint hostname. Maps to POSTGRES_HOST."
  value       = aws_db_instance.this.address
}

output "db_port" {
  description = "Connection port. Maps to POSTGRES_PORT."
  value       = aws_db_instance.this.port
}

output "db_name" {
  description = "Initial database name. Maps to POSTGRES_DB."
  value       = aws_db_instance.this.db_name
}

output "security_group_id" {
  description = "Security group attached to the DB instance."
  value       = aws_security_group.db.id
}
