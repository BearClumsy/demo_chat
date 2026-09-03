locals {
  common_user_data_vars = {
    aws_region         = var.aws_region
    cluster_name       = var.name
    ssm_prefix         = local.ssm_prefix
    kubernetes_version = var.kubernetes_version
    kms_key_id         = aws_kms_key.cluster.key_id
    api_endpoint       = aws_lb.api.dns_name
  }
}

# --- Control plane -------------------------------------------------------------------------

resource "aws_launch_template" "control_plane" {
  name_prefix            = "${var.name}-cp-"
  image_id               = var.node_ami_id
  instance_type          = var.control_plane_instance_type
  vpc_security_group_ids = [aws_security_group.control_plane.id]

  iam_instance_profile {
    arn = aws_iam_instance_profile.control_plane.arn
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  block_device_mappings {
    device_name = var.root_device_name

    ebs {
      volume_size = var.control_plane_root_volume_gb
      volume_type = "gp3"
      encrypted   = true
    }
  }

  block_device_mappings {
    device_name = "/dev/xvdf"

    ebs {
      volume_size = var.etcd_volume_gb
      volume_type = "gp3"
      encrypted   = true
    }
  }

  user_data = base64encode(templatefile("${path.module}/templates/user-data-controlplane.sh.tftpl", merge(local.common_user_data_vars, {
    pod_cidr             = var.pod_cidr
    snapshot_bucket      = aws_s3_bucket.this.bucket
    single_control_plane = local.single_control_plane
  })))

  tag_specifications {
    resource_type = "instance"
    tags = merge(var.tags, {
      Name       = "${var.name}-cp"
      "k8s-role" = "control-plane"
    })
  }

  tags = merge(var.tags, { Name = "${var.name}-cp" })
}

resource "aws_autoscaling_group" "control_plane" {
  name                      = "${var.name}-cp"
  min_size                  = var.control_plane_desired_count
  max_size                  = var.control_plane_desired_count
  desired_capacity          = var.control_plane_desired_count
  vpc_zone_identifier       = var.app_subnet_ids
  health_check_type         = "EC2"
  health_check_grace_period = 600
  target_group_arns         = [aws_lb_target_group.api.arn]
  suspended_processes       = var.control_plane_suspended_processes

  launch_template {
    id      = aws_launch_template.control_plane.id
    version = "$Latest"
  }

  dynamic "tag" {
    for_each = merge(var.tags, {
      "Name"     = "${var.name}-cp"
      "k8s-role" = "control-plane"
    })

    content {
      key                 = tag.key
      value               = tag.value
      propagate_at_launch = true
    }
  }

  lifecycle {
    create_before_destroy = true
  }
}

# --- Workers ----------------------------------------------------------------------------

resource "aws_launch_template" "worker" {
  name_prefix            = "${var.name}-worker-"
  image_id               = var.node_ami_id
  instance_type          = var.worker_instance_type
  vpc_security_group_ids = [aws_security_group.worker.id]

  iam_instance_profile {
    arn = aws_iam_instance_profile.worker.arn
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  block_device_mappings {
    device_name = var.root_device_name

    ebs {
      volume_size = var.worker_root_volume_gb
      volume_type = "gp3"
      encrypted   = true
    }
  }

  user_data = base64encode(templatefile("${path.module}/templates/user-data-worker.sh.tftpl", merge(local.common_user_data_vars, {
    worker_asg_name = "${var.name}-worker"
  })))

  tag_specifications {
    resource_type = "instance"
    tags = merge(var.tags, {
      Name       = "${var.name}-worker"
      "k8s-role" = "worker"
    })
  }

  tags = merge(var.tags, { Name = "${var.name}-worker" })
}

resource "aws_autoscaling_group" "worker" {
  name                      = "${var.name}-worker"
  min_size                  = var.worker_min_size
  max_size                  = var.worker_max_size
  desired_capacity          = var.worker_desired_count
  vpc_zone_identifier       = var.app_subnet_ids
  health_check_type         = "EC2"
  health_check_grace_period = 600
  target_group_arns         = var.ingress_target_group_arns

  launch_template {
    id      = aws_launch_template.worker.id
    version = "$Latest"
  }

  # Hold the node in Pending:Wait until kubeadm join succeeds, so a half-joined node never
  # enters the ingress target group. user-data-worker completes or abandons this hook.
  initial_lifecycle_hook {
    name                 = "wait-for-kubeadm-join"
    lifecycle_transition = "autoscaling:EC2_INSTANCE_LAUNCHING"
    default_result       = "ABANDON"
    heartbeat_timeout    = 900
  }

  dynamic "tag" {
    for_each = merge(var.tags, {
      "Name"                                  = "${var.name}-worker"
      "k8s-role"                              = "worker"
      "k8s.io/cluster-autoscaler/enabled"     = "true"
      "k8s.io/cluster-autoscaler/${var.name}" = "owned"
    })

    content {
      key                 = tag.key
      value               = tag.value
      propagate_at_launch = true
    }
  }

  lifecycle {
    create_before_destroy = true
  }
}
