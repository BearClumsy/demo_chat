# Node IAM. Bedrock creds reach pods via the worker instance profile (no IRSA on kubeadm).

data "aws_iam_policy_document" "node_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

# --- Control-plane role ----------------------------------------------------------------------

resource "aws_iam_role" "control_plane" {
  name               = "${var.name}-cp"
  assume_role_policy = data.aws_iam_policy_document.node_assume.json
  tags               = var.tags
}

resource "aws_iam_role_policy_attachment" "control_plane_ssm" {
  role       = aws_iam_role.control_plane.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy_document" "control_plane" {
  statement {
    sid = "ManageJoinParameters"
    actions = [
      "ssm:PutParameter",
      "ssm:GetParameter",
      "ssm:GetParameters",
      "ssm:GetParametersByPath",
      "ssm:DeleteParameter",
      "ssm:AddTagsToResource",
    ]
    resources = ["arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter${local.ssm_prefix}/*"]
  }

  statement {
    sid = "UseClusterKey"
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:GenerateDataKey",
    ]
    resources = [aws_kms_key.cluster.arn]
  }

  statement {
    sid = "EtcdSnapshotsAndDeployObjects"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:ListBucket",
    ]
    resources = [
      aws_s3_bucket.this.arn,
      "${aws_s3_bucket.this.arn}/etcd-snapshots/*",
      "${aws_s3_bucket.this.arn}/deploy/*",
    ]
  }

  statement {
    sid       = "DescribeSelf"
    actions   = ["ec2:DescribeInstances", "autoscaling:DescribeAutoScalingGroups"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "control_plane" {
  name   = "${var.name}-cp"
  role   = aws_iam_role.control_plane.id
  policy = data.aws_iam_policy_document.control_plane.json
}

resource "aws_iam_instance_profile" "control_plane" {
  name = "${var.name}-cp"
  role = aws_iam_role.control_plane.name
}

# --- Worker role ---------------------------------------------------------------------------

resource "aws_iam_role" "worker" {
  name               = "${var.name}-worker"
  assume_role_policy = data.aws_iam_policy_document.node_assume.json
  tags               = var.tags
}

resource "aws_iam_role_policy_attachment" "worker_ssm" {
  role       = aws_iam_role.worker.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy_document" "worker" {
  statement {
    sid       = "ReadJoinParameters"
    actions   = ["ssm:GetParameter", "ssm:GetParameters"]
    resources = ["arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter${local.ssm_prefix}/*"]
  }

  statement {
    sid       = "DecryptJoinParameters"
    actions   = ["kms:Decrypt"]
    resources = [aws_kms_key.cluster.arn]
  }

  statement {
    sid       = "EcrAuthToken"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid = "EcrPull"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer",
    ]
    resources = var.ecr_repository_arns
  }

  statement {
    sid     = "InvokeBedrockModels"
    actions = ["bedrock:InvokeModel", "bedrock:InvokeModelWithResponseStream"]
    resources = [
      "arn:aws:bedrock:${var.aws_region}::foundation-model/${var.titan_embed_model_id}",
      "arn:aws:bedrock:${var.aws_region}::foundation-model/${var.chat_model_id}",
    ]
  }

  statement {
    sid = "ClusterAutoscalerRead"
    actions = [
      "autoscaling:DescribeAutoScalingGroups",
      "autoscaling:DescribeAutoScalingInstances",
      "autoscaling:DescribeLaunchConfigurations",
      "autoscaling:DescribeScalingActivities",
      "autoscaling:DescribeTags",
      "ec2:DescribeImages",
      "ec2:DescribeInstanceTypes",
      "ec2:GetInstanceTypesFromInstanceRequirements",
      "ec2:DescribeLaunchTemplateVersions",
    ]
    resources = ["*"]
  }

  statement {
    sid = "ClusterAutoscalerWrite"
    actions = [
      "autoscaling:SetDesiredCapacity",
      "autoscaling:TerminateInstanceInAutoScalingGroup",
    ]
    resources = ["*"]

    condition {
      test     = "StringEquals"
      variable = "autoscaling:ResourceTag/k8s.io/cluster-autoscaler/${var.name}"
      values   = ["owned"]
    }
  }

  # Boot-critical: the worker completes its own EC2_INSTANCE_LAUNCHING hook once kubeadm join
  # succeeds (or abandons it on failure), so this must not be tag-conditioned.
  statement {
    sid = "CompleteOwnLifecycleHook"
    actions = [
      "autoscaling:CompleteLifecycleAction",
      "autoscaling:RecordLifecycleActionHeartbeat",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "worker" {
  name   = "${var.name}-worker"
  role   = aws_iam_role.worker.id
  policy = data.aws_iam_policy_document.worker.json
}

resource "aws_iam_instance_profile" "worker" {
  name = "${var.name}-worker"
  role = aws_iam_role.worker.name
}
