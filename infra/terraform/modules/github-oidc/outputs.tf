output "deploy_role_arn" {
  description = "IAM role ARN the GitHub Actions deploy workflows assume via OIDC."
  value       = aws_iam_role.deploy.arn
}

output "oidc_provider_arn" {
  description = "ARN of the GitHub OIDC provider (created here or passed through)."
  value       = local.oidc_provider_arn
}
