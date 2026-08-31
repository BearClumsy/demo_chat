output "alb_arn" {
  description = "ARN of the ALB."
  value       = aws_lb.this.arn
}

output "alb_dns_name" {
  description = "Public DNS name of the ALB."
  value       = aws_lb.this.dns_name
}

output "alb_security_group_id" {
  description = "Security group attached to the ALB."
  value       = aws_security_group.alb.id
}

output "target_group_arn" {
  description = "ARN of the application target group the ECS service registers into."
  value       = aws_lb_target_group.app.arn
}
