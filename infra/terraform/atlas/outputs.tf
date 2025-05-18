output "mongodb_uri" {
  description = "URI completa para conectar con credenciales y base app"
  sensitive   = true

  value = format(
    "mongodb+srv://%s:%s@%s/app?retryWrites=true&w=majority",
    mongodbatlas_database_user.app_user.username,
    mongodbatlas_database_user.app_user.password,
    replace(
      mongodbatlas_serverless_instance.franquicias_free.connection_strings_standard_srv,
      "mongodb+srv://", ""
    )
  )
}
