output "cluster_arn" {
  description = "ARN of the ECS cluster."
  value       = aws_ecs_cluster.this.arn
}

output "service_name" {
  description = "Name of the ECS service (for `aws ecs update-service`)."
  value       = aws_ecs_service.app.name
}

output "task_definition_arn" {
  description = "ARN of the initial task definition revision."
  value       = aws_ecs_task_definition.app.arn
}

output "task_security_group_id" {
  description = "Security group attached to the running tasks. Data tiers allow ingress from this."
  value       = aws_security_group.task.id
}

output "log_group_name" {
  description = "CloudWatch log group the container logs to."
  value       = aws_cloudwatch_log_group.app.name
}
