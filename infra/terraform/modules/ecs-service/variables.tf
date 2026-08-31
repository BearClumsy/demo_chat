variable "name" {
  description = "Name prefix for the cluster, service, task definition and log group."
  type        = string
}

variable "aws_region" {
  description = "Region, used for the awslogs driver."
  type        = string
}

variable "vpc_id" {
  description = "VPC the task security group lives in."
  type        = string
}

variable "app_subnet_ids" {
  description = "Private application subnets the Fargate tasks run in."
  type        = list(string)
}

variable "alb_target_group_arn" {
  description = "Target group the service registers tasks into."
  type        = string
}

variable "alb_security_group_id" {
  description = "ALB security group; the task SG allows ingress from it on the app port only."
  type        = string
}

variable "image" {
  description = "Fully-qualified container image (ECR repo URL + tag). TODO: set once ECR exists."
  type        = string
}

variable "app_port" {
  description = "Container port the server listens on."
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Container health-check path."
  type        = string
  default     = "/actuator/health"
}

variable "desired_count" {
  description = "Number of tasks to run."
  type        = number
  default     = 2
}

variable "cpu" {
  description = "Task CPU units (1024 = 1 vCPU)."
  type        = number
  default     = 1024
}

variable "memory" {
  description = "Task memory in MiB."
  type        = number
  default     = 2048
}

variable "log_retention_days" {
  description = "CloudWatch log group retention."
  type        = number
  default     = 30
}

variable "task_role_arn" {
  description = "IAM role assumed by the running container (Bedrock invoke)."
  type        = string
}

variable "execution_role_arn" {
  description = "IAM role the ECS agent uses to pull the image and read task secrets."
  type        = string
}

variable "plaintext_env" {
  description = "Non-secret container environment. Mirrors application-staging.properties keys."
  type        = map(string)
  default     = {}
}

variable "secret_arns" {
  description = "Map of container env var name -> Secrets Manager secret ARN (passwords, API keys)."
  type        = map(string)
  default     = {}
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
