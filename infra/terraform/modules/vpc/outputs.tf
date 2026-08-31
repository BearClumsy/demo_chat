output "vpc_id" {
  description = "ID of the VPC."
  value       = aws_vpc.this.id
}

output "vpc_cidr_block" {
  description = "Primary CIDR block of the VPC."
  value       = aws_vpc.this.cidr_block
}

output "public_subnet_ids" {
  description = "IDs of the public (ALB / NAT) subnets."
  value       = aws_subnet.public[*].id
}

output "app_subnet_ids" {
  description = "IDs of the private application (ECS Fargate) subnets."
  value       = aws_subnet.app[*].id
}

output "data_subnet_ids" {
  description = "IDs of the private data (RDS / Qdrant / MSK) subnets."
  value       = aws_subnet.data[*].id
}
