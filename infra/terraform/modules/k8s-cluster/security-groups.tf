# Two node security groups. Intra-cluster rules are added as separate
# aws_vpc_security_group_ingress_rule resources so the CP<->worker references don't form a cycle.

resource "aws_security_group" "control_plane" {
  name        = "${var.name}-cp"
  description = "kubeadm control-plane: API server, etcd, kubelet, scheduler, controller-manager."
  vpc_id      = var.vpc_id

  tags = merge(var.tags, { Name = "${var.name}-cp" })
}

resource "aws_security_group" "worker" {
  name        = "${var.name}-worker"
  description = "kubeadm workers: kubelet, ingress NodePorts from the ALB, Calico VXLAN."
  vpc_id      = var.vpc_id

  tags = merge(var.tags, { Name = "${var.name}-worker" })
}

# --- Control-plane ingress -----------------------------------------------------------------

resource "aws_vpc_security_group_ingress_rule" "cp_api_from_vpc" {
  security_group_id = aws_security_group.control_plane.id
  description       = "kube-apiserver from within the VPC (workers, NLB health checks)"
  ip_protocol       = "tcp"
  from_port         = 6443
  to_port           = 6443
  cidr_ipv4         = var.vpc_cidr
}

resource "aws_vpc_security_group_ingress_rule" "cp_api_from_admin" {
  security_group_id = aws_security_group.control_plane.id
  description       = "kube-apiserver from the break-glass admin CIDR"
  ip_protocol       = "tcp"
  from_port         = 6443
  to_port           = 6443
  cidr_ipv4         = var.admin_cidr
}

resource "aws_vpc_security_group_ingress_rule" "cp_ssh_from_admin" {
  security_group_id = aws_security_group.control_plane.id
  description       = "SSH from the break-glass admin CIDR (prefer SSM Session Manager)"
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
  cidr_ipv4         = var.admin_cidr
}

resource "aws_vpc_security_group_ingress_rule" "cp_etcd_self" {
  security_group_id            = aws_security_group.control_plane.id
  description                  = "etcd peer/client between control-plane nodes"
  ip_protocol                  = "tcp"
  from_port                    = 2379
  to_port                      = 2380
  referenced_security_group_id = aws_security_group.control_plane.id
}

resource "aws_vpc_security_group_ingress_rule" "cp_controlplane_components_self" {
  security_group_id            = aws_security_group.control_plane.id
  description                  = "scheduler + controller-manager health/metrics between control-plane nodes"
  ip_protocol                  = "tcp"
  from_port                    = 10257
  to_port                      = 10259
  referenced_security_group_id = aws_security_group.control_plane.id
}

resource "aws_vpc_security_group_ingress_rule" "cp_kubelet_self" {
  security_group_id            = aws_security_group.control_plane.id
  description                  = "kubelet API from control-plane nodes"
  ip_protocol                  = "tcp"
  from_port                    = 10250
  to_port                      = 10250
  referenced_security_group_id = aws_security_group.control_plane.id
}

resource "aws_vpc_security_group_ingress_rule" "cp_kubelet_from_workers" {
  security_group_id            = aws_security_group.control_plane.id
  description                  = "kubelet API from worker nodes (metrics-server, logs, exec)"
  ip_protocol                  = "tcp"
  from_port                    = 10250
  to_port                      = 10250
  referenced_security_group_id = aws_security_group.worker.id
}

resource "aws_vpc_security_group_ingress_rule" "cp_vxlan_self" {
  security_group_id            = aws_security_group.control_plane.id
  description                  = "Calico VXLAN from control-plane nodes"
  ip_protocol                  = "udp"
  from_port                    = 4789
  to_port                      = 4789
  referenced_security_group_id = aws_security_group.control_plane.id
}

resource "aws_vpc_security_group_ingress_rule" "cp_vxlan_from_workers" {
  security_group_id            = aws_security_group.control_plane.id
  description                  = "Calico VXLAN from worker nodes"
  ip_protocol                  = "udp"
  from_port                    = 4789
  to_port                      = 4789
  referenced_security_group_id = aws_security_group.worker.id
}

resource "aws_vpc_security_group_egress_rule" "cp_all" {
  security_group_id = aws_security_group.control_plane.id
  description       = "All outbound"
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

# --- Worker ingress ----------------------------------------------------------------------

resource "aws_vpc_security_group_ingress_rule" "worker_ingress_nodeports" {
  for_each = toset([for p in var.ingress_node_ports : tostring(p)])

  security_group_id            = aws_security_group.worker.id
  description                  = "ingress-nginx NodePort ${each.value} from the ALB"
  ip_protocol                  = "tcp"
  from_port                    = tonumber(each.value)
  to_port                      = tonumber(each.value)
  referenced_security_group_id = var.alb_security_group_id
}

resource "aws_vpc_security_group_ingress_rule" "worker_ssh_from_admin" {
  security_group_id = aws_security_group.worker.id
  description       = "SSH from the break-glass admin CIDR (prefer SSM Session Manager)"
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
  cidr_ipv4         = var.admin_cidr
}

resource "aws_vpc_security_group_ingress_rule" "worker_kubelet_from_cp" {
  security_group_id            = aws_security_group.worker.id
  description                  = "kubelet API from control-plane nodes"
  ip_protocol                  = "tcp"
  from_port                    = 10250
  to_port                      = 10250
  referenced_security_group_id = aws_security_group.control_plane.id
}

resource "aws_vpc_security_group_ingress_rule" "worker_kubelet_self" {
  security_group_id            = aws_security_group.worker.id
  description                  = "kubelet + NodePort health checks between worker nodes"
  ip_protocol                  = "tcp"
  from_port                    = 10250
  to_port                      = 10250
  referenced_security_group_id = aws_security_group.worker.id
}

resource "aws_vpc_security_group_ingress_rule" "worker_vxlan_self" {
  security_group_id            = aws_security_group.worker.id
  description                  = "Calico VXLAN between worker nodes"
  ip_protocol                  = "udp"
  from_port                    = 4789
  to_port                      = 4789
  referenced_security_group_id = aws_security_group.worker.id
}

resource "aws_vpc_security_group_ingress_rule" "worker_vxlan_from_cp" {
  security_group_id            = aws_security_group.worker.id
  description                  = "Calico VXLAN from control-plane nodes"
  ip_protocol                  = "udp"
  from_port                    = 4789
  to_port                      = 4789
  referenced_security_group_id = aws_security_group.control_plane.id
}

resource "aws_vpc_security_group_egress_rule" "worker_all" {
  security_group_id = aws_security_group.worker.id
  description       = "All outbound (data tier, Bedrock, ECR, Secrets Manager, SSM)"
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}
