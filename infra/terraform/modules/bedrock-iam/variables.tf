variable "name" {
  description = "Name prefix for the IAM roles and policies."
  type        = string
}

variable "aws_region" {
  description = "Region the Bedrock foundation models are invoked in."
  type        = string
}

variable "titan_embed_model_id" {
  description = "Bedrock embedding model id. Matches spring.ai.bedrock.titan.embedding.model."
  type        = string
  default     = "amazon.titan-embed-text-v2:0"
}

variable "chat_model_id" {
  description = "Bedrock chat model id. Matches spring.ai.bedrock.converse.chat.model."
  type        = string
  default     = "anthropic.claude-3-5-haiku-20241022-v1:0"
}

variable "secret_arns" {
  description = "Secrets Manager ARNs the execution role may read for task secrets. Empty is allowed."
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
