# Non-secret production values. `terraform validate` needs none of this; `terraform plan` needs the
# TODO-marked variables in variables.tf (server_image, acm_certificate_arn, qdrant_ami_id,
# rds_password, task_secret_arns) supplied via a secret mechanism, not this file.

region      = "eu-central-1"
name_prefix = "demo-chat-prod"

vpc_cidr = "10.30.0.0/16"
azs      = ["eu-central-1a", "eu-central-1b", "eu-central-1c"]

public_subnet_cidrs = ["10.30.0.0/24", "10.30.1.0/24", "10.30.2.0/24"]
app_subnet_cidrs    = ["10.30.10.0/24", "10.30.11.0/24", "10.30.12.0/24"]
data_subnet_cidrs   = ["10.30.20.0/24", "10.30.21.0/24", "10.30.22.0/24"]

desired_count = 3
