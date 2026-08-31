provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = "demo-chat"
      Environment = "prod"
      ManagedBy   = "terraform"
    }
  }
}
