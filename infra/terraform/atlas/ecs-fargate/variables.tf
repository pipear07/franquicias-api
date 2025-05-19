variable "cluster_name" {
  type    = string
  default = "franquicias-cluster"
}

variable "region" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "image" {
  type = string
}

variable "env_vars" {
  type = map(string)
}

variable "container_port" {
  type    = number
  default = 8080
}
