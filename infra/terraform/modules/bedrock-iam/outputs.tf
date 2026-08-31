output "task_role_arn" {
  description = "IAM role ARN for the running container (Bedrock invoke)."
  value       = aws_iam_role.task.arn
}

output "execution_role_arn" {
  description = "IAM role ARN for the ECS agent (image pull + task secrets)."
  value       = aws_iam_role.execution.arn
}
