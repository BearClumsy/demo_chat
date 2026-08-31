resource "aws_security_group" "qdrant" {
  name        = "${var.name}-qdrant"
  description = "Qdrant gRPC (6334) and REST (6333) from the application tier only."
  vpc_id      = var.vpc_id

  ingress {
    description = "Qdrant gRPC"
    from_port   = 6334
    to_port     = 6334
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  ingress {
    description = "Qdrant REST"
    from_port   = 6333
    to_port     = 6333
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

  tags = merge(var.tags, { Name = "${var.name}-qdrant" })
}

resource "aws_instance" "qdrant" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [aws_security_group.qdrant.id]
  key_name               = var.key_name

  root_block_device {
    volume_size = var.root_volume_size_gb
    volume_type = "gp3"
    encrypted   = true
  }

  tags = merge(var.tags, { Name = "${var.name}-qdrant" })
}

resource "aws_ebs_volume" "qdrant_data" {
  availability_zone = aws_instance.qdrant.availability_zone
  size              = var.data_volume_size_gb
  type              = "gp3"
  encrypted         = true

  tags = merge(var.tags, { Name = "${var.name}-qdrant-data" })
}

resource "aws_volume_attachment" "qdrant_data" {
  device_name = "/dev/xvdf"
  volume_id   = aws_ebs_volume.qdrant_data.id
  instance_id = aws_instance.qdrant.id
}
