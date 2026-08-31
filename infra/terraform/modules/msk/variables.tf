variable "name" {
  description = "Name prefix for the MSK cluster and its security group."
  type        = string
}

variable "vpc_id" {
  description = "VPC the security group lives in."
  type        = string
}

variable "subnet_ids" {
  description = "Private data subnets for the brokers. Count must divide number_of_broker_nodes."
  type        = list(string)
}

variable "allowed_cidr_blocks" {
  description = <<-EOT
    CIDR blocks allowed to reach the brokers on 9092/9094 — the application subnet CIDRs.
    TODO: tighten to a security-group reference (see the same note on the rds-postgres module).
  EOT
  type        = list(string)
}

variable "kafka_version" {
  description = "MSK Kafka version."
  type        = string
  default     = "3.6.0"
}

variable "broker_instance_type" {
  description = "Broker instance type."
  type        = string
  default     = "kafka.m5.large"
}

variable "number_of_broker_nodes" {
  description = "Total brokers. Must be a multiple of length(subnet_ids)."
  type        = number
  default     = 2
}

variable "ebs_volume_size_gb" {
  description = "Per-broker EBS storage."
  type        = number
  default     = 100
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
