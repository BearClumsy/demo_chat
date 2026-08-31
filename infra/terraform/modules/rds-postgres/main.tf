resource "aws_db_subnet_group" "this" {
  name       = "${var.name}-pg"
  subnet_ids = var.data_subnet_ids

  tags = merge(var.tags, { Name = "${var.name}-pg" })
}

resource "aws_security_group" "db" {
  name        = "${var.name}-pg"
  description = "Postgres 5432 from the application tier only."
  vpc_id      = var.vpc_id

  ingress {
    description = "Postgres from the app subnets"
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, { Name = "${var.name}-pg" })
}

resource "aws_db_instance" "this" {
  identifier     = "${var.name}-pg"
  engine         = "postgres"
  engine_version = var.engine_version
  instance_class = var.instance_class

  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true

  # Only the database is provisioned here; the demo_chat schema inside it is created by the app's
  # Flyway migration on first boot (V1__create_users_table.sql hardcodes it).
  db_name  = var.db_name
  username = var.username
  password = var.password # TODO: switch to manage_master_user_password + Secrets Manager

  db_subnet_group_name    = aws_db_subnet_group.this.name
  vpc_security_group_ids  = [aws_security_group.db.id]
  multi_az                = var.multi_az
  backup_retention_period = var.backup_retention_days

  deletion_protection       = var.deletion_protection
  skip_final_snapshot       = false
  final_snapshot_identifier = "${var.name}-pg-final"

  tags = merge(var.tags, { Name = "${var.name}-pg" })
}
