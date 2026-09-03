# Self-managed kubeadm cluster on EC2.
#
# Bootstrap: control-plane and worker user-data race on an SSM "init-lock" parameter written
# WITHOUT --overwrite; the single winner runs `kubeadm init --upload-certs` against the internal
# API NLB DNS and publishes join commands to SSM SecureString. Everyone else polls and joins.
# A systemd timer on each control-plane node refreshes the (short-lived) join tokens and the
# certificate-key every 6h so ASG-replaced nodes months later can still join; a second timer
# ships etcd snapshots to S3.
#
# CI never talks to the API server directly: the deploy workflow stages rendered manifests in
# this module's S3 bucket and runs the `${var.name}-kubectl-apply` SSM document on a
# control-plane node (tag k8s-role=control-plane). No inbound 6443 from the internet.

data "aws_caller_identity" "current" {}

locals {
  ssm_prefix          = "/demo-chat/${var.env}/k8s"
  single_control_plane = var.control_plane_desired_count == 1
}

# --- KMS: encrypts the SSM SecureString join material and the etcd snapshots ------------------

resource "aws_kms_key" "cluster" {
  description             = "${var.name} kubeadm join material and etcd snapshots"
  deletion_window_in_days = var.kms_deletion_window_days
  enable_key_rotation     = true

  tags = merge(var.tags, { Name = "${var.name}-k8s" })
}

resource "aws_kms_alias" "cluster" {
  name          = "alias/${var.name}-k8s"
  target_key_id = aws_kms_key.cluster.key_id
}

# --- S3: deploy/ holds rendered manifests for the SSM apply; etcd-snapshots/ holds backups ----

resource "aws_s3_bucket" "this" {
  bucket        = "${var.name}-k8s-${data.aws_caller_identity.current.account_id}"
  force_destroy = false

  tags = merge(var.tags, { Name = "${var.name}-k8s" })
}

resource "aws_s3_bucket_public_access_block" "this" {
  bucket = aws_s3_bucket.this.id

  block_public_acls       = true
  block_public_policy      = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_versioning" "this" {
  bucket = aws_s3_bucket.this.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "this" {
  bucket = aws_s3_bucket.this.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.cluster.arn
    }
    bucket_key_enabled = true
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "this" {
  bucket = aws_s3_bucket.this.id

  rule {
    id     = "expire-rendered-manifests"
    status = "Enabled"

    filter {
      prefix = "deploy/"
    }

    expiration {
      days = var.deploy_object_expiry_days
    }

    noncurrent_version_expiration {
      noncurrent_days = 1
    }
  }

  rule {
    id     = "expire-etcd-snapshots"
    status = "Enabled"

    filter {
      prefix = "etcd-snapshots/"
    }

    expiration {
      days = var.snapshot_retention_days
    }
  }
}

data "aws_iam_policy_document" "bucket" {
  statement {
    sid       = "DenyInsecureTransport"
    effect    = "Deny"
    actions   = ["s3:*"]
    resources = [aws_s3_bucket.this.arn, "${aws_s3_bucket.this.arn}/*"]

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_s3_bucket_policy" "this" {
  bucket = aws_s3_bucket.this.id
  policy = data.aws_iam_policy_document.bucket.json
}

# --- SSM: the only path CI has to the cluster --------------------------------------------------

resource "aws_ssm_document" "deploy" {
  name            = "${var.name}-kubectl-apply"
  document_type   = "Command"
  document_format = "YAML"

  content = yamlencode({
    schemaVersion = "2.2"
    description    = "Pull rendered demo_chat manifests from S3 and kubectl apply on this control-plane node."
    parameters = {
      S3Uri = {
        type           = "String"
        description    = "s3://<bucket>/deploy/<sha>/ prefix holding manifest.yaml and (optionally) secret.yaml"
        allowedPattern = "^s3://[a-z0-9.-]+/deploy/[A-Za-z0-9._/-]+/?$"
      }
    }
    mainSteps = [
      {
        action = "aws:runShellScript"
        name   = "kubectlApply"
        inputs = {
          timeoutSeconds = "600"
          runCommand = [
            "set -euo pipefail",
            "export KUBECONFIG=/etc/kubernetes/admin.conf",
            "work=$(mktemp -d)",
            "trap 'rm -rf \"$work\"' EXIT",
            "aws s3 cp '{{ S3Uri }}' \"$work/\" --recursive --region ${var.aws_region}",
            "exec 9>/var/run/demo-chat-deploy.lock",
            "flock -w 300 9",
            "kubectl apply -f \"$work/manifest.yaml\"",
            "[ -f \"$work/secret.yaml\" ] && kubectl apply -f \"$work/secret.yaml\" || true",
            "kubectl -n demo-chat rollout status deploy/demo-chat-server --timeout=300s",
            "kubectl -n demo-chat rollout status deploy/demo-chat-client --timeout=300s",
          ]
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_ssm_document" "rollback" {
  name            = "${var.name}-kubectl-rollback"
  document_type   = "Command"
  document_format = "YAML"

  content = yamlencode({
    schemaVersion = "2.2"
    description    = "Roll the demo_chat Deployments back to their previous revision."
    mainSteps = [
      {
        action = "aws:runShellScript"
        name   = "kubectlRollback"
        inputs = {
          timeoutSeconds = "360"
          runCommand = [
            "set -euo pipefail",
            "export KUBECONFIG=/etc/kubernetes/admin.conf",
            "kubectl -n demo-chat rollout undo deploy/demo-chat-server",
            "kubectl -n demo-chat rollout undo deploy/demo-chat-client",
            "kubectl -n demo-chat rollout status deploy/demo-chat-server --timeout=300s",
            "kubectl -n demo-chat rollout status deploy/demo-chat-client --timeout=300s",
          ]
        }
      }
    ]
  })

  tags = var.tags
}

# --- VPC interface endpoints so SSM works even when NAT egress is degraded --------------------

resource "aws_security_group" "vpce" {
  name        = "${var.name}-k8s-vpce"
  description = "HTTPS from the cluster subnets to the SSM interface endpoints."
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTPS from the application subnets"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = var.app_subnet_cidrs
  }

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, { Name = "${var.name}-k8s-vpce" })
}

resource "aws_vpc_endpoint" "ssm" {
  for_each = toset(["ssm", "ssmmessages", "ec2messages"])

  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.${each.value}"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.app_subnet_ids
  security_group_ids  = [aws_security_group.vpce.id]
  private_dns_enabled = true

  tags = merge(var.tags, { Name = "${var.name}-vpce-${each.value}" })
}
