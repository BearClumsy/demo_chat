variable "name" {
  description = "Name prefix for the ALB, target group and security group."
  type        = string
}

variable "vpc_id" {
  description = "VPC the ALB and target group live in."
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnets for the internet-facing ALB (one per AZ the workers span)."
  type        = list(string)
}

variable "certificate_arn" {
  description = "ACM certificate ARN for the HTTPS listener."
  type        = string
  # TODO: issue a cert for the environment hostname.
}

variable "ingress_http_node_port" {
  description = "NodePort the ingress-nginx controller Service pins for HTTP. ALB forwards here."
  type        = number
  default     = 30080
}

variable "ingress_healthz_node_port" {
  description = "NodePort the ingress-nginx controller Service pins for /healthz. ALB health-checks here."
  type        = number
  default     = 30254
}

variable "idle_timeout_seconds" {
  description = "ALB idle timeout. Kept high so buffered-SSE responses are not cut off."
  type        = number
  default     = 300
}

variable "deregistration_delay_seconds" {
  description = "Target draining time on deploy / scale-in. = the app's 300s SSE stream ceiling."
  type        = number
  default     = 300
}

variable "route53_zone_id" {
  description = "Optional Route53 hosted zone id. When set with hostname, an alias record is created."
  type        = string
  default     = null
}

variable "hostname" {
  description = "Optional FQDN to alias at the ALB (e.g. chat.staging.example.com)."
  type        = string
  default     = null
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
