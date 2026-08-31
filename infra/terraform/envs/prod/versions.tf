terraform {
  required_version = ">= 1.9.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
  }

  # Remote state. Commented out until the state bucket and lock table exist — they are created
  # once, out of band (see docs/wiki/Plan/infrastructure.md). Until then this env uses local state,
  # and `terraform init -backend=false` in CI skips the backend entirely.
  #
  # backend "s3" {
  #   bucket         = "demo-chat-tfstate"
  #   key            = "prod/terraform.tfstate"
  #   region         = "eu-central-1"
  #   dynamodb_table = "demo-chat-tflock"
  #   encrypt        = true
  # }
}
