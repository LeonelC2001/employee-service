################################################################################
# main.tf — AWS EKS Cluster (Dev + Prod namespaces in ONE cluster)
#
# Cost estimate (us-east-1, 2026):
#   EKS control plane:  $0.10/hr  → ~$73/mo
#   2x t3.medium nodes: $0.0416/hr each → ~$61/mo
#   RDS MySQL db.t3.micro (prod): ~$15/mo
#   Total estimate:     ~$149/mo   (vs ~$400–$600/mo for ROSA/OpenShift)
#
# Usage:
#   terraform init
#   terraform plan -var-file=terraform.tfvars
#   terraform apply -var-file=terraform.tfvars
################################################################################

terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
  }

  # Remote state in S3 — create the bucket manually first
  backend "s3" {
    bucket = "invex-employee-tfstate-143009744272"
    key    = "employee-service/terraform.tfstate"
    region = "us-east-2"
  }
}

provider "aws" {
  region = var.aws_region
}

###############################################################################
# Variables
###############################################################################
variable "aws_region"    { default = "us-east-1" }
variable "cluster_name"  { default = "invex-eks" }
variable "cluster_version" { default = "1.30" }
variable "db_password"   { sensitive = true }
variable "app_admin_password" { sensitive = true }

###############################################################################
# VPC (2 public + 2 private subnets, 2 AZs)
###############################################################################
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "${var.cluster_name}-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["${var.aws_region}a", "${var.aws_region}b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway   = true
  single_nat_gateway   = true     # Save cost — use 1 NAT; use multiple for HA
  enable_dns_hostnames = true

  public_subnet_tags = {
    "kubernetes.io/role/elb" = 1
  }
  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = 1
  }
}

###############################################################################
# EKS Cluster
###############################################################################
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = var.cluster_name
  cluster_version = var.cluster_version

  vpc_id                   = module.vpc.vpc_id
  subnet_ids               = module.vpc.private_subnets
  cluster_endpoint_public_access = true

eks_managed_node_groups = {
  general = {
    instance_types       = ["t3.small"]
    min_size             = 2
    max_size             = 6
    desired_size         = 2
    ami_type             = "AL2_x86_64"
    force_update_version = true

    labels = {
      role = "general"
    }
  }
}

}

###############################################################################
# RDS MySQL (Production only)
###############################################################################
resource "aws_db_subnet_group" "this" {
  name       = "${var.cluster_name}-db-subnet"
  subnet_ids = module.vpc.private_subnets
}

resource "aws_security_group" "rds" {
  name   = "${var.cluster_name}-rds-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = [module.vpc.vpc_cidr_block]   # Only from within VPC
  }
}

resource "aws_db_instance" "mysql_prod" {
  identifier        = "employee-service-prod"
  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = "db.t3.micro"              # ~$15/mo
  allocated_storage = 20
  storage_type      = "gp2"

  db_name  = "employeedb"
  username = "employee"
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  skip_final_snapshot     = false
  final_snapshot_identifier = "employee-prod-final-snapshot"
  backup_retention_period = 0
  deletion_protection     = true
  multi_az                = false               # Enable for HA prod (cost 2x)
  storage_encrypted       = true
}

###############################################################################
# ECR Repository
###############################################################################
resource "aws_ecr_repository" "employee_service" {
  name                 = "employee-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  lifecycle {
    prevent_destroy = true
  }
}

# Keep only last 10 images to save storage cost
resource "aws_ecr_lifecycle_policy" "employee_service" {
  repository = aws_ecr_repository.employee_service.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep last 10 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 10
      }
      action = { type = "expire" }
    }]
  })
}

###############################################################################
# AWS Secrets Manager
###############################################################################
resource "aws_secretsmanager_secret" "db_url" {
  name = "employee-service/prod/db-url"
}
resource "aws_secretsmanager_secret_version" "db_url" {
  secret_id     = aws_secretsmanager_secret.db_url.id
  secret_string = "jdbc:mysql://${aws_db_instance.mysql_prod.endpoint}/employeedb?useSSL=true&requireSSL=true"
}

resource "aws_secretsmanager_secret" "db_user" {
  name = "employee-service/prod/db-user"
}
resource "aws_secretsmanager_secret_version" "db_user" {
  secret_id     = aws_secretsmanager_secret.db_user.id
  secret_string = "employee"
}

resource "aws_secretsmanager_secret" "db_password" {
  name = "employee-service/prod/db-password"
}
resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password
}

###############################################################################
# Outputs
###############################################################################
output "cluster_name"    { value = module.eks.cluster_name }
output "ecr_registry"    { value = aws_ecr_repository.employee_service.repository_url }
output "rds_endpoint"    { value = aws_db_instance.mysql_prod.endpoint }
output "cluster_endpoint" { value = module.eks.cluster_endpoint }
