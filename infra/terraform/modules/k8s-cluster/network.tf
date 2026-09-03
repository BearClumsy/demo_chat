# Internal NLB in front of kube-apiserver. Its DNS name is the stable --control-plane-endpoint
# baked into every kubeconfig and join command, so a replaced control-plane node keeps the same
# address. Internal only — CI reaches the cluster through SSM, not this endpoint.

resource "aws_lb" "api" {
  name                             = "${var.name}-api"
  internal                         = true
  load_balancer_type               = "network"
  subnets                          = var.app_subnet_ids
  enable_cross_zone_load_balancing = true

  tags = merge(var.tags, { Name = "${var.name}-api" })
}

resource "aws_lb_target_group" "api" {
  name        = "${var.name}-api"
  port        = 6443
  protocol    = "TCP"
  target_type = "instance"
  vpc_id      = var.vpc_id

  health_check {
    protocol            = "HTTPS"
    path                = "/readyz"
    port                = "6443"
    interval            = 10
    healthy_threshold   = 2
    unhealthy_threshold = 2
  }

  tags = merge(var.tags, { Name = "${var.name}-api" })
}

resource "aws_lb_listener" "api" {
  load_balancer_arn = aws_lb.api.arn
  port              = 6443
  protocol          = "TCP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api.arn
  }
}
