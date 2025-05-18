provider "aws" {
  region = var.aws_region
}

# Data source para la VPC existente
data "aws_vpc" "existing" {
  id = var.vpc_id
}

# Data sources para las subnets públicas
data "aws_subnet" "public_a" {
  id = var.subnet_a_id
}

data "aws_subnet" "public_b" {
  id = var.subnet_b_id
}

data "aws_subnet" "public_c" {
  id = var.subnet_c_id
}
