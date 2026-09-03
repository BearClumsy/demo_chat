variable "server_repository_name" {
  description = "ECR repository name for the Spring Boot server image."
  type        = string
  default     = "demo-chat-server"
}

variable "client_repository_name" {
  description = "ECR repository name for the nginx client image."
  type        = string
  default     = "demo-chat-client"
}

variable "image_tag_mutability" {
  description = <<-EOT
    IMMUTABLE (default) or MUTABLE. The deploy workflows push unique tags (git sha / release tag),
    so immutability is safe and stops a tag being silently repointed.
  EOT
  type        = string
  default     = "IMMUTABLE"
}

variable "keep_last_images" {
  description = "Lifecycle policy: how many images to retain per repository before expiring the oldest."
  type        = number
  default     = 20
}

variable "untagged_expiry_days" {
  description = "Lifecycle policy: expire untagged images older than this many days."
  type        = number
  default     = 7
}

variable "force_delete" {
  description = "Allow `terraform destroy` to remove a repository that still contains images."
  type        = bool
  default     = false
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
