resource "aws_ecs_cluster" "this" {
  name = var.name

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = var.tags
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${var.name}"
  retention_in_days = var.log_retention_days

  tags = var.tags
}

resource "aws_security_group" "task" {
  name        = "${var.name}-task"
  description = "App tasks: ingress from the ALB on the app port only; all egress."
  vpc_id      = var.vpc_id

  ingress {
    description     = "From the ALB"
    from_port       = var.app_port
    to_port         = var.app_port
    protocol        = "tcp"
    security_groups = [var.alb_security_group_id]
  }

  egress {
    description = "Bedrock, Qdrant, RDS, Keyspaces, MSK, Secrets Manager"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, { Name = "${var.name}-task" })
}

locals {
  container_environment = [
    for k, v in var.plaintext_env : {
      name  = k
      value = v
    }
  ]

  container_secrets = [
    for k, arn in var.secret_arns : {
      name      = k
      valueFrom = arn
    }
  ]
}

resource "aws_ecs_task_definition" "app" {
  family                   = var.name
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = var.execution_role_arn
  task_role_arn            = var.task_role_arn

  container_definitions = jsonencode([
    {
      name        = "server"
      image       = var.image
      essential   = true
      environment = local.container_environment
      secrets     = local.container_secrets

      portMappings = [
        {
          containerPort = var.app_port
          protocol      = "tcp"
        }
      ]

      healthCheck = {
        command     = ["CMD-SHELL", "wget -q -O /dev/null http://localhost:${var.app_port}${var.health_check_path} || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 90
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "server"
        }
      }
    }
  ])

  tags = var.tags
}

resource "aws_ecs_service" "app" {
  name            = var.name
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.app_subnet_ids
    security_groups  = [aws_security_group.task.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.alb_target_group_arn
    container_name   = "server"
    container_port   = var.app_port
  }

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  # The deploy pipeline updates the task definition out of band (register + update-service).
  lifecycle {
    ignore_changes = [task_definition]
  }

  tags = var.tags
}
