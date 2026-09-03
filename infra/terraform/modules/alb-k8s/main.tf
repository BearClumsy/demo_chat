# Internet-facing ALB in front of the kubeadm workers. Unlike modules/alb (target_type = "ip",
# for Fargate), this registers the worker ASG by instance on the ingress-nginx HTTP NodePort and
# health-checks the controller's own /healthz NodePort. TLS terminates here; ingress-nginx runs
# HTTP-only with ssl-redirect off.

resource "aws_security_group" "alb" {
  name        = "${var.name}-alb-k8s"
  description = "Internet ingress to the ALB on 80/443; egress to the worker ingress NodePorts."
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTP (redirected to HTTPS)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "To the worker nodes' ingress NodePorts"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, { Name = "${var.name}-alb-k8s" })
}

resource "aws_lb" "this" {
  name               = "${var.name}-k8s"
  load_balancer_type = "application"
  internal           = false
  subnets            = var.public_subnet_ids
  security_groups    = [aws_security_group.alb.id]
  idle_timeout       = var.idle_timeout_seconds

  tags = merge(var.tags, { Name = "${var.name}-k8s" })
}

resource "aws_lb_target_group" "ingress" {
  name                 = "${var.name}-ingress"
  port                 = var.ingress_http_node_port
  protocol             = "HTTP"
  target_type          = "instance"
  vpc_id               = var.vpc_id
  deregistration_delay = var.deregistration_delay_seconds

  # externalTrafficPolicy: Local on the ingress-nginx DaemonSet means this NodePort answers only
  # on nodes with a ready controller pod, so the health check doubles as "controller ready here".
  health_check {
    path                = "/healthz"
    port                = tostring(var.ingress_healthz_node_port)
    protocol            = "HTTP"
    matcher             = "200"
    interval            = 15
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }

  tags = merge(var.tags, { Name = "${var.name}-ingress" })
}

resource "aws_lb_listener" "http_redirect" {
  load_balancer_arn = aws_lb.this.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.this.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.ingress.arn
  }
}

resource "aws_route53_record" "this" {
  count = var.route53_zone_id != null && var.hostname != null ? 1 : 0

  zone_id = var.route53_zone_id
  name    = var.hostname
  type    = "A"

  alias {
    name                   = aws_lb.this.dns_name
    zone_id                = aws_lb.this.zone_id
    evaluate_target_health = true
  }
}
