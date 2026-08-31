provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = "demo-chat"
      Environment = "staging"
      ManagedBy   = "terraform"
    }
  }
}
