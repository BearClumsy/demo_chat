variable "name" {
  description = "Name prefix for the deploy role."
  type        = string
}

variable "aws_region" {
  description = "Region, used when scoping the ssm:SendCommand instance ARNs."
  type        = string
}

variable "create_oidc_provider" {
  description = <<-EOT
    Create the token.actions.githubusercontent.com OIDC provider. It is an account-global
    singleton, so set this false in the second environment (or if the account already has one)
    and pass existing_oidc_provider_arn instead.
  EOT
  type        = bool
  default     = true
}

variable "existing_oidc_provider_arn" {
  description = "ARN of a pre-existing GitHub OIDC provider, used when create_oidc_provider is false."
  type        = string
  default     = null
}

variable "oidc_thumbprints" {
  description = <<-EOT
    SHA-1 thumbprints of the GitHub OIDC IdP certificate chain. AWS now validates GitHub's
    token signature against the library of trusted CAs, so this list is effectively vestigial,
    but the API still requires a non-empty value.
  EOT
  type        = list(string)
  default     = ["6938fd4d98bab03faadb97b34396831e3780aea1"]
}

variable "allowed_subjects" {
  description = <<-EOT
    OIDC `sub` claims allowed to assume the deploy role, as StringLike patterns. Built by the env
    root from var.github_org: staging trusts pushes to main + the staging Environment, prod trusts
    release tags + the production Environment (which carries the manual-approval gate).
  EOT
  type = list(string)
}

variable "ecr_repository_arns" {
  description = "ECR repository ARNs the deploy role may push to."
  type        = list(string)
}

variable "secret_arns" {
  description = "Secrets Manager ARNs the deploy role may read to render the k8s Secret at deploy time."
  type        = list(string)
  default     = []
}

variable "deploy_bucket_arn" {
  description = "ARN of the S3 bucket rendered manifests are staged in for the SSM apply step."
  type        = string
}

variable "ssm_document_arns" {
  description = "ARNs of the SSM documents the deploy role may run (the kubectl-apply wrapper)."
  type        = list(string)
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
