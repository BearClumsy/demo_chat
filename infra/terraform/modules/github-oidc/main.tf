# GitHub Actions -> AWS via OIDC, no long-lived keys. The deploy workflows assume this role to
# push images to ECR, read the three (four, incl. CASSANDRA_USER) deploy secrets, stage the
# rendered manifests in S3, and trigger the kubectl-apply SSM document on a control-plane node.

resource "aws_iam_openid_connect_provider" "github" {
  count = var.create_oidc_provider ? 1 : 0

  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = var.oidc_thumbprints

  tags = merge(var.tags, { Name = "${var.name}-github-oidc" })
}

locals {
  oidc_provider_arn = var.create_oidc_provider ? aws_iam_openid_connect_provider.github[0].arn : var.existing_oidc_provider_arn
}

data "aws_iam_policy_document" "assume" {
  statement {
    sid     = "GithubOidcAssume"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [local.oidc_provider_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = var.allowed_subjects
    }
  }
}

resource "aws_iam_role" "deploy" {
  name                 = "${var.name}-gha-deploy"
  assume_role_policy   = data.aws_iam_policy_document.assume.json
  max_session_duration = 3600
  tags                 = var.tags
}

data "aws_iam_policy_document" "deploy" {
  statement {
    sid       = "EcrAuthToken"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid = "EcrPushPull"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:CompleteLayerUpload",
      "ecr:DescribeImages",
      "ecr:GetDownloadUrlForLayer",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart",
    ]
    resources = var.ecr_repository_arns
  }

  statement {
    sid       = "ReadDeploySecrets"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = length(var.secret_arns) > 0 ? var.secret_arns : ["arn:aws:secretsmanager:${var.aws_region}:*:secret:demo-chat/*"]
  }

  statement {
    sid = "StageRenderedManifests"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:DeleteObject",
    ]
    resources = ["${var.deploy_bucket_arn}/*"]
  }

  statement {
    sid       = "RunDeployDocument"
    actions   = ["ssm:SendCommand"]
    resources = var.ssm_document_arns
  }

  statement {
    sid       = "TargetControlPlaneNodes"
    actions   = ["ssm:SendCommand"]
    resources = ["arn:aws:ec2:${var.aws_region}:*:instance/*"]

    condition {
      test     = "StringEquals"
      variable = "ssm:resourceTag/k8s-role"
      values   = ["control-plane"]
    }
  }

  statement {
    sid = "TrackDeployCommand"
    actions = [
      "ssm:GetCommandInvocation",
      "ssm:ListCommandInvocations",
      "ssm:DescribeInstanceInformation",
      "ec2:DescribeInstances",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "deploy" {
  name   = "${var.name}-gha-deploy"
  role   = aws_iam_role.deploy.id
  policy = data.aws_iam_policy_document.deploy.json
}
