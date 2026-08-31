variable "name" {
  description = "Name prefix for the ALB and its target group."
  type        = string
}

variable "vpc_id" {
  description = "VPC the ALB and target group live in."
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnets to attach the ALB to (one per AZ)."
  type        = list(string)
}

variable "certificate_arn" {
  description = "ACM certificate ARN for the HTTPS listener. TODO: issue a cert for the env hostname."
  type        = string
}

variable "app_port" {
  description = "Container port the Spring Boot server listens on."
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "ALB health-check path. permitAll in SecurityConfig."
  type        = string
  default     = "/actuator/health"
}

variable "idle_timeout_seconds" {
  description = "ALB idle timeout. Kept high so buffered-SSE responses are not cut off."
  type        = number
  default     = 300
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
