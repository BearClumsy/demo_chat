output "alb_dns_name" {
  description = "Public DNS name of the ingress ALB. Point the staging hostname at this."
  value       = module.alb_k8s.alb_dns_name
}

output "container_env" {
  description = <<-EOT
    The non-secret container environment Terraform resolves. Every key must line up with
    modules/server/src/main/resources/application-staging.properties and the ConfigMap in
    infra/k8s/manifest-staging.yaml. Secret keys (POSTGRES_PASSWORD, CASSANDRA_USER,
    CASSANDRA_PASSWORD, QDRANT_API_KEY) come from Secrets Manager via the deploy workflow and are
    intentionally absent here.
  EOT
  value       = local.plaintext_env
}

output "ecr_server_url" {
  description = "ECR repository URL for the server image (no tag)."
  value       = module.ecr.server_repository_url
}

output "ecr_client_url" {
  description = "ECR repository URL for the client image (no tag)."
  value       = module.ecr.client_repository_url
}

output "github_deploy_role_arn" {
  description = "IAM role the GitHub Actions deploy workflow assumes via OIDC."
  value       = module.github_oidc.deploy_role_arn
}

output "k8s_api_endpoint" {
  description = "Internal NLB DNS for kube-apiserver (--control-plane-endpoint)."
  value       = module.k8s_cluster.api_endpoint
}

output "k8s_worker_asg_name" {
  description = "Worker Auto Scaling group name."
  value       = module.k8s_cluster.worker_asg_name
}

output "k8s_deploy_bucket" {
  description = "S3 bucket the deploy workflow stages rendered manifests in."
  value       = module.k8s_cluster.deploy_bucket
}

output "k8s_ssm_deploy_document" {
  description = "SSM document name the deploy workflow runs on a control-plane node."
  value       = module.k8s_cluster.ssm_deploy_document_name
}

output "k8s_ssm_rollback_document" {
  description = "SSM document name the deploy workflow runs to roll back on smoke-test failure."
  value       = module.k8s_cluster.ssm_rollback_document_name
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
