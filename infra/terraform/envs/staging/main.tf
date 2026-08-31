locals {
  name = var.name_prefix

  tags = {
    Project     = "demo-chat"
    Environment = "staging"
  }

  # Non-secret container environment. Every key here must line up with a property in
  # modules/server/src/main/resources/application-staging.properties. Secret values
  # (POSTGRES_PASSWORD, CASSANDRA_PASSWORD, QDRANT_API_KEY) are injected separately from
  # Secrets Manager via var.task_secret_arns and are intentionally absent.
  plaintext_env = {
    SPRING_PROFILES_ACTIVE = "staging"

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

module "alb" {
  source = "../../modules/alb"

  name              = local.name
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids
  certificate_arn   = var.acm_certificate_arn
  tags              = local.tags
}

module "bedrock_iam" {
  source = "../../modules/bedrock-iam"

  name        = local.name
  aws_region  = var.region
  secret_arns = values(var.task_secret_arns)
  tags        = local.tags
}

module "rds_postgres" {
  source = "../../modules/rds-postgres"

  name                = local.name
  vpc_id              = module.vpc.vpc_id
  data_subnet_ids     = module.vpc.data_subnet_ids
  allowed_cidr_blocks = var.app_subnet_cidrs
  instance_class      = "db.t3.medium"
  multi_az            = false
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
  instance_type       = "t3.medium"
  allowed_cidr_blocks = var.app_subnet_cidrs
  tags                = local.tags
}

module "msk" {
  source = "../../modules/msk"

  name                   = local.name
  vpc_id                 = module.vpc.vpc_id
  subnet_ids             = module.vpc.data_subnet_ids
  allowed_cidr_blocks    = var.app_subnet_cidrs
  number_of_broker_nodes = 2
  tags                   = local.tags
}

module "ecs_service" {
  source = "../../modules/ecs-service"

  name                  = local.name
  aws_region            = var.region
  vpc_id                = module.vpc.vpc_id
  app_subnet_ids        = module.vpc.app_subnet_ids
  alb_target_group_arn  = module.alb.target_group_arn
  alb_security_group_id = module.alb.alb_security_group_id
  image                 = var.server_image
  desired_count         = var.desired_count
  cpu                   = 1024
  memory                = 2048
  task_role_arn         = module.bedrock_iam.task_role_arn
  execution_role_arn    = module.bedrock_iam.execution_role_arn
  plaintext_env         = local.plaintext_env
  secret_arns           = var.task_secret_arns
  tags                  = local.tags
}
