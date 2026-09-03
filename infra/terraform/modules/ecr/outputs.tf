output "server_repository_url" {
  description = "Push/pull URL for the server image repository (no tag)."
  value       = aws_ecr_repository.this["server"].repository_url
}

output "client_repository_url" {
  description = "Push/pull URL for the client image repository (no tag)."
  value       = aws_ecr_repository.this["client"].repository_url
}

output "repository_arns" {
  description = "ARNs of both repositories, for scoping the deploy role's ECR push policy."
  value       = [for r in aws_ecr_repository.this : r.arn]
}

output "registry_id" {
  description = "Registry (account) id hosting the repositories."
  value       = aws_ecr_repository.this["server"].registry_id
}
