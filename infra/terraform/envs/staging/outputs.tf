output "alb_dns_name" {
  description = "Public DNS name of the ALB. Point the staging hostname at this."
  value       = module.alb.alb_dns_name
}

output "container_env" {
  description = <<-EOT
    The non-secret container environment Terraform resolves for the ECS task. Every key must line
    up with modules/server/src/main/resources/application-staging.properties. Secret keys
    (POSTGRES_PASSWORD, CASSANDRA_PASSWORD, QDRANT_API_KEY) come from Secrets Manager via
    task_secret_arns and are intentionally absent here.
  EOT
  value       = local.plaintext_env
}

output "ecs_cluster_arn" {
  description = "ECS cluster ARN."
  value       = module.ecs_service.cluster_arn
}

output "ecs_service_name" {
  description = "ECS service name, for `aws ecs update-service --force-new-deployment`."
  value       = module.ecs_service.service_name
}

output "rds_endpoint" {
  description = "RDS Postgres endpoint hostname."
  value       = module.rds_postgres.db_host
}

output "qdrant_host" {
  description = "Private DNS of the Qdrant host."
  value       = module.qdrant.qdrant_host
}

output "msk_bootstrap_brokers_tls" {
  description = "MSK TLS bootstrap brokers."
  value       = module.msk.bootstrap_brokers_tls
}
