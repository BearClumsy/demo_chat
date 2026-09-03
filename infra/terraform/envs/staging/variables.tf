variable "region" {
  description = "AWS region for the staging environment."
  type        = string
  default     = "eu-central-1"
}

variable "name_prefix" {
  description = "Prefix for all resource names."
  type        = string
  default     = "demo-chat-staging"
}

variable "vpc_cidr" {
  description = "Primary CIDR for the staging VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "azs" {
  description = "Availability zones (2 for staging)."
  type        = list(string)
  default     = ["eu-central-1a", "eu-central-1b"]
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs, one per AZ."
  type        = list(string)
  default     = ["10.20.0.0/24", "10.20.1.0/24"]
}

variable "app_subnet_cidrs" {
  description = "Private application subnet CIDRs, one per AZ. Also the ingress allow-list for the data tier."
  type        = list(string)
  default     = ["10.20.10.0/24", "10.20.11.0/24"]
}

variable "data_subnet_cidrs" {
  description = "Private data subnet CIDRs, one per AZ."
  type        = list(string)
  default     = ["10.20.20.0/24", "10.20.21.0/24"]
}

variable "acm_certificate_arn" {
  description = "ACM certificate ARN for the ALB HTTPS listener."
  type        = string
  # TODO: issue a cert for the staging hostname.
}

variable "qdrant_ami_id" {
  description = "AMI id for the Qdrant EC2 host."
  type        = string
  # TODO: build or select an AL2023/Ubuntu AMI running the Qdrant container.
}

variable "node_ami_id" {
  description = "AMI id for the kubeadm control-plane and worker nodes."
  type        = string
  # TODO: bake an Ubuntu 24.04 / AL2023 image with containerd + kubeadm + the ecr-credential-provider.
}

variable "kubernetes_version" {
  description = "Kubernetes minor version installed on the nodes (kubeadm/kubelet/kubectl)."
  type        = string
  default     = "1.31"
}

variable "admin_cidr" {
  description = "CIDR allowed to reach the API server (6443) and SSH for break-glass."
  type        = string
  # TODO: a bastion / VPN CIDR. Never 0.0.0.0/0.
}

variable "github_org" {
  description = "GitHub org/owner that hosts the demo_chat repository (for the OIDC deploy role trust)."
  type        = string
  # TODO: set to the real org/owner login.
}

variable "rds_username" {
  description = "Master username for RDS Postgres."
  type        = string
  default     = "demo_chat"
}

variable "rds_password" {
  description = "Master password for RDS Postgres. Unset on purpose so a plan without a real secret fails fast."
  type        = string
  sensitive   = true
}

variable "task_secret_arns" {
  description = <<-EOT
    Map of container env var name -> Secrets Manager secret ARN. Keys mirror the secret entries in
    application-staging.properties plus CASSANDRA_USER (Amazon Keyspaces issues a username+password
    pair): POSTGRES_PASSWORD, CASSANDRA_USER, CASSANDRA_PASSWORD, QDRANT_API_KEY. The values feed
    the GitHub deploy role's secretsmanager:GetSecretValue scope; the deploy workflow reads them
    and renders the k8s Secret. Left empty here; populated once the secrets exist.
  EOT
  type    = map(string)
  default = {}
}
