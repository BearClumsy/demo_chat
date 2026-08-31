variable "name" {
  description = "Name prefix for the instance, volume and security group."
  type        = string
}

variable "vpc_id" {
  description = "VPC the security group lives in."
  type        = string
}

variable "subnet_id" {
  description = "Single private data subnet to place the Qdrant host in."
  type        = string
}

variable "ami_id" {
  description = <<-EOT
    AMI for the Qdrant host. No default so a plan without a real value fails fast.
    TODO: bake an AL2023/Ubuntu image running the qdrant/qdrant container, or attach user_data.
  EOT
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type."
  type        = string
  default     = "t3.medium"
}

variable "key_name" {
  description = "Optional EC2 key pair for break-glass SSH. Prefer SSM Session Manager."
  type        = string
  default     = null
}

variable "root_volume_size_gb" {
  description = "Root EBS volume size."
  type        = number
  default     = 20
}

variable "data_volume_size_gb" {
  description = "Dedicated EBS volume for the Qdrant storage directory."
  type        = number
  default     = 50
}

variable "allowed_cidr_blocks" {
  description = <<-EOT
    CIDR blocks allowed to reach Qdrant on 6333/6334 — the application subnet CIDRs.
    TODO: tighten to a security-group reference (see the same note on the rds-postgres module).
  EOT
  type        = list(string)
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
