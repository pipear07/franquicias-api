variable "aws_region" {
  description = "Región AWS donde están los recursos"
  type        = string
  default     = "us-east-2"
}

variable "vpc_id" {
  description = "ID de la VPC existente"
  type        = string
  default     = "vpc-036aea0e84d497cba"
}

variable "subnet_a_id" {
  description = "ID de la primera subnet pública"
  type        = string
  default     = "subnet-04e0b7808ee7d3088"
}

variable "subnet_b_id" {
  description = "ID de la segunda subnet pública"
  type        = string
  default     = "subnet-06160a399b2d0d631"
}

variable "subnet_c_id" {
  description = "ID de la tercera subnet pública"
  type        = string
  default     = "subnet-08c0ed5fc13597c6f"
}
