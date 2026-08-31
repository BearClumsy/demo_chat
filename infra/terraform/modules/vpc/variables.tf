variable "name" {
  description = "Name prefix for all VPC resources (e.g. demo-chat-staging)."
  type        = string
}

variable "cidr_block" {
  description = "Primary IPv4 CIDR block for the VPC."
  type        = string
}

variable "azs" {
  description = "Availability zones to spread the subnets across (2+ recommended)."
  type        = list(string)
}

variable "public_subnet_cidrs" {
  description = "One CIDR per AZ for the public (ALB / NAT) tier."
  type        = list(string)
}

variable "app_subnet_cidrs" {
  description = "One CIDR per AZ for the private application (ECS Fargate) tier."
  type        = list(string)
}

variable "data_subnet_cidrs" {
  description = "One CIDR per AZ for the private data (RDS / Qdrant / MSK) tier."
  type        = list(string)
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
