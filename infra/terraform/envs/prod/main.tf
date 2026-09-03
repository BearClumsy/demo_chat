locals {
  name = var.name_prefix

  tags = {
    Project     = "demo-chat"
    Environment = "prod"
  }

  # Non-secret container environment. Every key here must line up with a property in
  # modules/server/src/main/resources/application-prod.properties and with the ConfigMap in
  # infra/k8s/manifest-prod.yaml. Secret values (POSTGRES_PASSWORD, CASSANDRA_USER,
  # CASSANDRA_PASSWORD, QDRANT_API_KEY) come from Secrets Manager, are rendered into the k8s
  # Secret by the deploy workflow, and are intentionally absent here.
  plaintext_env = {
    SPRING_PROFILES_ACTIVE = "prod"

    POSTGRES_HOST = module.rds_postgres.db_host
    POSTGRES_PORT = tostring(module.rds_postgres.db_port)
    POSTGRES_DB   = module.rds_postgres.db_name
    POSTGRES_USER = var.rds_username

    CASSANDRA_CONTACT_POINTS   = "cassandra.${var.region}.amazonaws.com"
    CASSANDRA_PORT             = "9142"
    CASSANDRA_LOCAL_DATACENTER = var.region
    CASSANDRA_KEYSPACE         = module.keyspaces.keyspace_name

    QDRANT_HOST              = module.qdrant.qdrant_host
    QDRANT_PORT              = "6334"
    QDRANT_USE_TLS           = "false"
    QDRANT_INITIALIZE_SCHEMA = "false"

    KAFKA_BOOTSTRAP_SERVERS = module.msk.bootstrap_brokers_tls
  }

  # OIDC subjects allowed to assume the deploy role: release tags and the production GitHub
  # Environment (which carries the manual-approval gate).
  github_deploy_subjects = [
    "repo:${var.github_org}/demo_chat:ref:refs/tags/v*",
    "repo:${var.github_org}/demo_chat:environment:production",
  ]
}

module "vpc" {
  source = "../../modules/vpc"

  name                = local.name
  cidr_block          = var.vpc_cidr
  azs                 = var.azs
  public_subnet_cidrs = var.public_subnet_cidrs
  app_subnet_cidrs    = var.app_subnet_cidrs
  data_subnet_cidrs   = var.data_subnet_cidrs
  tags                = local.tags
}

module "ecr" {
  source = "../../modules/ecr"

  tags = local.tags
}

module "alb_k8s" {
  source = "../../modules/alb-k8s"

  name              = local.name
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids
  certificate_arn   = var.acm_certificate_arn
  tags              = local.tags
}

module "rds_postgres" {
  source = "../../modules/rds-postgres"

  name                = local.name
  vpc_id              = module.vpc.vpc_id
  data_subnet_ids     = module.vpc.data_subnet_ids
  allowed_cidr_blocks = var.app_subnet_cidrs
  instance_class      = "db.m6g.large"
  multi_az            = true
  username            = var.rds_username
  password            = var.rds_password
  tags                = local.tags
}

module "keyspaces" {
  source = "../../modules/keyspaces"

  keyspace_name = "demo_chat"
  tags          = local.tags
}

module "qdrant" {
  source = "../../modules/qdrant-ec2"

  name                = local.name
  vpc_id              = module.vpc.vpc_id
  subnet_id           = module.vpc.data_subnet_ids[0]
  ami_id              = var.qdrant_ami_id
  instance_type       = "m6i.large"
  data_volume_size_gb = 100
  allowed_cidr_blocks = var.app_subnet_cidrs
  tags                = local.tags
}

module "msk" {
  source = "../../modules/msk"

  name                   = local.name
  vpc_id                 = module.vpc.vpc_id
  subnet_ids             = module.vpc.data_subnet_ids
  allowed_cidr_blocks    = var.app_subnet_cidrs
  number_of_broker_nodes = 3
  tags                   = local.tags
}

module "k8s_cluster" {
  source = "../../modules/k8s-cluster"

  name       = local.name
  env        = "prod"
  aws_region = var.region

  vpc_id           = module.vpc.vpc_id
  vpc_cidr         = var.vpc_cidr
  app_subnet_ids   = module.vpc.app_subnet_ids
  app_subnet_cidrs = var.app_subnet_cidrs

  node_ami_id        = var.node_ami_id
  kubernetes_version = var.kubernetes_version
  admin_cidr         = var.admin_cidr

  control_plane_desired_count = 3
  control_plane_instance_type = "m6i.large"
  worker_instance_type        = "m6i.xlarge"
  worker_desired_count        = 3
  worker_min_size             = 3
  worker_max_size             = 6

  alb_security_group_id     = module.alb_k8s.alb_security_group_id
  ingress_target_group_arns = [module.alb_k8s.target_group_arn]
  ecr_repository_arns       = module.ecr.repository_arns

  tags = local.tags
}

module "github_oidc" {
  source = "../../modules/github-oidc"

  name       = local.name
  aws_region = var.region

  # If staging and prod share one AWS account, set this to false and pass
  # existing_oidc_provider_arn — the provider is an account-global singleton.
  create_oidc_provider = true
  allowed_subjects     = local.github_deploy_subjects

  ecr_repository_arns = module.ecr.repository_arns
  secret_arns         = values(var.task_secret_arns)
  deploy_bucket_arn   = module.k8s_cluster.deploy_bucket_arn
  ssm_document_arns   = module.k8s_cluster.ssm_document_arns

  tags = local.tags
}
