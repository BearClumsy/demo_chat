output "api_endpoint" {
  description = "Internal NLB DNS name for kube-apiserver (the --control-plane-endpoint)."
  value       = aws_lb.api.dns_name
}

output "api_server_url" {
  description = "https URL of the API server, for a kubeconfig built on a bastion inside the VPC."
  value       = "https://${aws_lb.api.dns_name}:6443"
}

output "control_plane_asg_name" {
  description = "Control-plane Auto Scaling group name."
  value       = aws_autoscaling_group.control_plane.name
}

output "worker_asg_name" {
  description = "Worker Auto Scaling group name (cluster-autoscaler target)."
  value       = aws_autoscaling_group.worker.name
}

output "control_plane_security_group_id" {
  description = "Security group attached to control-plane nodes."
  value       = aws_security_group.control_plane.id
}

output "worker_security_group_id" {
  description = "Security group attached to worker nodes. Data-tier SGs may allow ingress from this."
  value       = aws_security_group.worker.id
}

output "control_plane_role_arn" {
  description = "IAM role ARN of control-plane nodes."
  value       = aws_iam_role.control_plane.arn
}

output "worker_role_arn" {
  description = "IAM role ARN of worker nodes (Bedrock invoke, ECR pull)."
  value       = aws_iam_role.worker.arn
}

output "kms_key_arn" {
  description = "KMS key protecting the SSM join parameters and etcd snapshots."
  value       = aws_kms_key.cluster.arn
}

output "deploy_bucket" {
  description = "S3 bucket the deploy workflow stages rendered manifests in (deploy/ prefix)."
  value       = aws_s3_bucket.this.bucket
}

output "deploy_bucket_arn" {
  description = "ARN of the deploy/snapshot bucket."
  value       = aws_s3_bucket.this.arn
}

output "ssm_deploy_document_name" {
  description = "SSM document the deploy workflow runs on a control-plane node to kubectl apply."
  value       = aws_ssm_document.deploy.name
}

output "ssm_deploy_document_arn" {
  description = "ARN of the kubectl-apply SSM document, for scoping the deploy role."
  value       = aws_ssm_document.deploy.arn
}

output "ssm_rollback_document_name" {
  description = "SSM document that rolls the Deployments back to their previous revision."
  value       = aws_ssm_document.rollback.name
}

output "ssm_document_arns" {
  description = "Both SSM document ARNs (deploy + rollback), for the deploy role's SendCommand scope."
  value       = [aws_ssm_document.deploy.arn, aws_ssm_document.rollback.arn]
}

output "ssm_parameter_prefix" {
  description = "SSM parameter path prefix holding the kubeadm join material."
  value       = local.ssm_prefix
}
