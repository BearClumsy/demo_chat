output "qdrant_host" {
  description = "Private DNS name of the Qdrant host. Maps to QDRANT_HOST."
  value       = aws_instance.qdrant.private_dns
}

output "qdrant_private_ip" {
  description = "Private IP of the Qdrant host."
  value       = aws_instance.qdrant.private_ip
}

output "security_group_id" {
  description = "Security group attached to the Qdrant host."
  value       = aws_security_group.qdrant.id
}

output "instance_id" {
  description = "EC2 instance ID."
  value       = aws_instance.qdrant.id
}
