variable "name" {
  description = "Name prefix for the DB instance, subnet group and security group."
  type        = string
}

variable "vpc_id" {
  description = "VPC the DB security group lives in."
  type        = string
}

variable "data_subnet_ids" {
  description = "Private data subnets for the DB subnet group (2+ AZ)."
  type        = list(string)
}

variable "allowed_cidr_blocks" {
  description = <<-EOT
    CIDR blocks allowed to reach Postgres on 5432 — the application subnet CIDRs.
    TODO: tighten to a security-group reference once the env root owns a shared app SG
    (referencing the ecs-service task SG here would create a module dependency cycle with
    the container environment wiring).
  EOT
  type        = list(string)
}

variable "engine_version" {
  description = "Postgres major/minor version."
  type        = string
  default     = "16"
}

variable "instance_class" {
  description = "RDS instance class."
  type        = string
  default     = "db.t3.medium"
}

variable "allocated_storage" {
  description = "Initial storage in GiB."
  type        = number
  default     = 20
}

variable "max_allocated_storage" {
  description = "Storage autoscaling ceiling in GiB."
  type        = number
  default     = 100
}

variable "multi_az" {
  description = "Whether to run a standby in a second AZ."
  type        = bool
  default     = false
}

variable "db_name" {
  description = "Initial database name. The app's Flyway V1 hardcodes schema demo_chat inside it."
  type        = string
  default     = "demo_chat"
}

variable "username" {
  description = "Master username."
  type        = string
  default     = "demo_chat"
}

variable "password" {
  description = "Master password. Left without a default so a plan without a real secret fails fast."
  type        = string
  sensitive   = true
}

variable "backup_retention_days" {
  description = "Automated backup retention."
  type        = number
  default     = 7
}

variable "deletion_protection" {
  description = "Block accidental deletion of the instance."
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
