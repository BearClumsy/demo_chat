variable "name" {
  description = "Name prefix for every resource (e.g. demo-chat-staging)."
  type        = string
}

variable "env" {
  description = "Environment slug (staging | prod). Used in the SSM parameter path."
  type        = string
}

variable "aws_region" {
  description = "Region the cluster runs in."
  type        = string
}

variable "vpc_id" {
  description = "VPC the cluster and its security groups live in."
  type        = string
}

variable "vpc_cidr" {
  description = "VPC CIDR, allowed to reach the API server (covers NLB health checks)."
  type        = string
}

variable "app_subnet_ids" {
  description = "Private application subnets for the control-plane and worker nodes and the internal API NLB."
  type        = list(string)
}

variable "app_subnet_cidrs" {
  description = "CIDRs of the application subnets, used for the SSM interface-endpoint security group."
  type        = list(string)
}

variable "node_ami_id" {
  description = <<-EOT
    AMI for the control-plane and worker nodes. No default so a plan without a real value fails
    fast. TODO: an Ubuntu 24.04 or AL2023 image; the user-data installs containerd + kubeadm and
    (workers) the ecr-credential-provider binary.
  EOT
  type = string
}

variable "root_device_name" {
  description = "Root block device name for node_ami_id (/dev/sda1 for Ubuntu, /dev/xvda for AL2023)."
  type        = string
  default     = "/dev/sda1"
}

variable "kubernetes_version" {
  description = "Kubernetes minor version to install and hold (kubeadm/kubelet/kubectl)."
  type        = string
  default     = "1.31"
}

variable "pod_cidr" {
  description = "Pod network CIDR for kubeadm --pod-network-cidr and Calico. Must not overlap the VPC."
  type        = string
  default     = "192.168.0.0/16"
}

variable "control_plane_instance_type" {
  description = "Instance type for control-plane nodes."
  type        = string
  default     = "t3.large"
}

variable "control_plane_desired_count" {
  description = "Control-plane node count. 1 for staging (single etcd), 3 for prod (quorum)."
  type        = number
  default     = 1
}

variable "control_plane_root_volume_gb" {
  description = "Root volume size for control-plane nodes."
  type        = number
  default     = 40
}

variable "etcd_volume_gb" {
  description = "Dedicated gp3 volume for /var/lib/etcd (fsync-latency sensitive)."
  type        = number
  default     = 20
}

variable "control_plane_suspended_processes" {
  description = <<-EOT
    ASG processes to suspend on the control-plane group. Defaults keep a transient health-check
    blip from auto-terminating an etcd member; on prod, control-plane replacement is a manual
    runbook (etcdctl member remove) regardless.
  EOT
  type    = list(string)
  default = ["AZRebalance", "ReplaceUnhealthy"]
}

variable "worker_instance_type" {
  description = "Instance type for worker nodes (sized for the server HPA ceiling + ingress DaemonSet)."
  type        = string
  default     = "m6i.large"
}

variable "worker_desired_count" {
  description = "Initial worker node count."
  type        = number
  default     = 2
}

variable "worker_min_size" {
  description = "Worker ASG minimum (cluster-autoscaler lower bound)."
  type        = number
  default     = 2
}

variable "worker_max_size" {
  description = "Worker ASG maximum (cluster-autoscaler upper bound)."
  type        = number
  default     = 4
}

variable "worker_root_volume_gb" {
  description = "Root volume size for worker nodes (image cache + ephemeral)."
  type        = number
  default     = 60
}

variable "ingress_node_ports" {
  description = "NodePorts the ingress-nginx Service pins: HTTP then healthz. Opened on the worker SG from the ALB SG."
  type        = list(number)
  default     = [30080, 30254]
}

variable "ingress_target_group_arns" {
  description = "Target group ARNs from module.alb-k8s the worker ASG registers into (the ingress NodePort)."
  type        = list(string)
  default     = []
}

variable "alb_security_group_id" {
  description = "Security group of the ingress ALB; the worker SG allows the ingress NodePorts from it."
  type        = string
}

variable "admin_cidr" {
  description = <<-EOT
    CIDR allowed to reach the API server (6443) and SSH directly, for break-glass. No default so a
    plan without a real value fails fast. TODO: a bastion/VPN CIDR, never 0.0.0.0/0.
  EOT
  type = string
}

variable "ecr_repository_arns" {
  description = "ECR repository ARNs the worker node role may pull (no imagePullSecrets)."
  type        = list(string)
}

variable "titan_embed_model_id" {
  description = "Bedrock embedding model id for the worker node Bedrock-invoke policy."
  type        = string
  default     = "amazon.titan-embed-text-v2:0"
}

variable "chat_model_id" {
  description = "Bedrock chat model id for the worker node Bedrock-invoke policy."
  type        = string
  default     = "anthropic.claude-3-5-haiku-20241022-v1:0"
}

variable "kms_deletion_window_days" {
  description = "Deletion window for the cluster KMS key (join material + etcd snapshots)."
  type        = number
  default     = 7
}

variable "deploy_object_expiry_days" {
  description = "Lifecycle expiry for rendered manifests staged under deploy/ in the cluster bucket."
  type        = number
  default     = 2
}

variable "snapshot_retention_days" {
  description = "Lifecycle expiry for etcd snapshots under etcd-snapshots/ in the cluster bucket."
  type        = number
  default     = 14
}

variable "tags" {
  description = "Tags applied to every resource."
  type        = map(string)
  default     = {}
}
