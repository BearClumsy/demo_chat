data "aws_iam_policy_document" "task_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "bedrock_invoke" {
  statement {
    sid     = "InvokeBedrockModels"
    actions = ["bedrock:InvokeModel", "bedrock:InvokeModelWithResponseStream"]

    resources = [
      "arn:aws:bedrock:${var.aws_region}::foundation-model/${var.titan_embed_model_id}",
      "arn:aws:bedrock:${var.aws_region}::foundation-model/${var.chat_model_id}",
    ]
  }
}

# Role assumed by the running container.
resource "aws_iam_role" "task" {
  name               = "${var.name}-task"
  assume_role_policy = data.aws_iam_policy_document.task_assume.json
  tags               = var.tags
}

resource "aws_iam_role_policy" "bedrock" {
  name   = "${var.name}-bedrock"
  role   = aws_iam_role.task.id
  policy = data.aws_iam_policy_document.bedrock_invoke.json
}

# Role the ECS agent uses to pull the image and inject task secrets.
resource "aws_iam_role" "execution" {
  name               = "${var.name}-exec"
  assume_role_policy = data.aws_iam_policy_document.task_assume.json
  tags               = var.tags
}

resource "aws_iam_role_policy_attachment" "execution_managed" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "secrets_read" {
  count = length(var.secret_arns) > 0 ? 1 : 0

  statement {
    sid       = "ReadTaskSecrets"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = var.secret_arns
  }
}

resource "aws_iam_role_policy" "execution_secrets" {
  count = length(var.secret_arns) > 0 ? 1 : 0

  name   = "${var.name}-exec-secrets"
  role   = aws_iam_role.execution.id
  policy = data.aws_iam_policy_document.secrets_read[0].json
}
