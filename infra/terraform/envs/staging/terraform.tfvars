# Non-secret staging values. `terraform validate` needs none of this; `terraform plan` needs the
# TODO-marked variables in variables.tf (server_image, acm_certificate_arn, qdrant_ami_id,
# rds_password, task_secret_arns) supplied via a secret mechanism, not this file.

region      = "eu-central-1"
name_prefix = "demo-chat-staging"

vpc_cidr = "10.20.0.0/16"
azs      = ["eu-central-1a", "eu-central-1b"]

public_subnet_cidrs = ["10.20.0.0/24", "10.20.1.0/24"]
app_subnet_cidrs    = ["10.20.10.0/24", "10.20.11.0/24"]
data_subnet_cidrs   = ["10.20.20.0/24", "10.20.21.0/24"]

desired_count = 2
