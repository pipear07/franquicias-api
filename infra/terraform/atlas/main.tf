terraform {
  required_providers {
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 1.34"
    }
  }
}

provider "mongodbatlas" {
  public_key  = var.atlas_public_key
  private_key = var.atlas_private_key
}

resource "mongodbatlas_serverless_instance" "franquicias_free" {
  project_id                           = var.project_id
  name                                 = var.cluster_name
  provider_settings_provider_name      = "SERVERLESS"
  provider_settings_backing_provider_name = var.serverless_provider
  provider_settings_region_name        = var.serverless_region
}

resource "mongodbatlas_database_user" "app_user" {
  project_id         = var.project_id
  username           = var.db_username
  password           = var.db_password
  auth_database_name = "admin"

  roles {
    role_name     = "readWrite"
    database_name = var.db_name
  }
}
