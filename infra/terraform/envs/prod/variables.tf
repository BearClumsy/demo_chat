variable "region" {
  description = "AWS region for the production environment."
  type        = string
  default     = "eu-central-1"
}

variable "name_prefix" {
  description = "Prefix for all resource names."
  type        = string
  default     = "demo-chat-prod"
}

variable "vpc_cidr" {
  description = "Primary CIDR for the production VPC."
  type        = string
  default     = "10.30.0.0/16"
}

variable "azs" {
  description = "Availability zones (3 for production)."
  type        = list(string)
  default     = ["eu-central-1a", "eu-central-1b", "eu-central-1c"]
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs, one per AZ."
  type        = list(string)
  default     = ["10.30.0.0/24", "10.30.1.0/24", "10.30.2.0/24"]
}

variable "app_subnet_cidrs" {
  description = "Private application subnet CIDRs, one per AZ. Also the ingress allow-list for the data tier."
  type        = list(string)
  default     = ["10.30.10.0/24", "10.30.11.0/24", "10.30.12.0/24"]
}

variable "data_subnet_cidrs" {
  description = "Private data subnet CIDRs, one per AZ."
  type        = list(string)
  default     = ["10.30.20.0/24", "10.30.21.0/24", "10.30.22.0/24"]
}

variable "server_image" {
  description = "Container image for the Spring Boot server (ECR repo URL + tag)."
  type        = string
  # TODO: <account>.dkr.ecr.<region>.amazonaws.com/demo-chat-server:<release-tag> once ECR exists.
}

variable "acm_certificate_arn" {
  description = "ACM certificate ARN for the ALB HTTPS listener."
  type        = string
  # TODO: issue a cert for the production hostname.
}

variable "qdrant_ami_id" {
  description = "AMI id for the Qdrant EC2 host."
  type        = string
  # TODO: build or select an AL2023/Ubuntu AMI running the Qdrant container.
}

variable "rds_username" {
  description = "Master username for RDS Postgres."
  type        = string
  default     = "demo_chat"
}

variable "rds_password" {
  description = "Master password for RDS Postgres. Unset on purpose so a plan without a real secret fails fast."
  type        = string
  sensitive   = true
}

variable "task_secret_arns" {
  description = <<-EOT
    Map of container env var name -> Secrets Manager secret ARN. Expected keys mirror the
    secret entries in application-prod.properties: POSTGRES_PASSWORD, CASSANDRA_PASSWORD,
    QDRANT_API_KEY. Left empty here; populated once the secrets exist.
  EOT
  type        = map(string)
  default     = {}
}

variable "desired_count" {
  description = "Number of ECS tasks."
  type        = number
  default     = 3
}
