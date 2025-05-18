variable "atlas_public_key" {
  description = "Public API Key de MongoDB Atlas"
  type        = string
}

variable "atlas_private_key" {
  description = "Private API Key de MongoDB Atlas"
  type        = string
}

variable "project_id" {
  description = "ID del proyecto MongoDB Atlas"
  type        = string
}

variable "cluster_name" {
  description = "Nombre de la instancia serverless"
  type        = string
  default     = "franquicias-free"
}

variable "serverless_provider" {
  description = "Proveedor cloud para Serverless Free Tier (AWS, GCP o AZURE)"
  type        = string
  default     = "AWS"
}

variable "serverless_region" {
  description = "Región para Serverless Free Tier (por ejemplo US_EAST_1)"
  type        = string
  default     = "US_EAST_1"
}

variable "db_username" {
  description = "Usuario de MongoDB para la aplicación"
  type        = string
}

variable "db_password" {
  description = "Password para el usuario de MongoDB"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Nombre de la base de datos en Atlas"
  type        = string
  default     = "franquicias"
}
