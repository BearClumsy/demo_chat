output "alb_dns_name" {
  description = "Public DNS name of the ALB. Point the environment hostname at this."
  value       = aws_lb.this.dns_name
}

output "alb_zone_id" {
  description = "Hosted zone id of the ALB, for an external alias record."
  value       = aws_lb.this.zone_id
}

output "alb_security_group_id" {
  description = "ALB security group. The worker SG allows the ingress NodePorts from this."
  value       = aws_security_group.alb.id
}

output "target_group_arn" {
  description = "Ingress target group ARN. The worker ASG registers into this."
  value       = aws_lb_target_group.ingress.arn
}
