# infra/terraform/atlas/ecs-fargate/terraform.tfvars

region     = "us-east-2"
vpc_id     = "vpc-036aea0e84d497cba"
subnet_ids = [
  "subnet-04e0b7808ee7d3088",
  "subnet-06160a399b2d0d631",
  "subnet-08c0ed5fc13597c6f",
]

image = "pipear07/franquicias-api:latest"

env_vars = {
  SPRING_DATA_MONGODB_URI = "mongodb+srv://pipear07:Afam2030@cluster0.abzvzny.mongodb.net/franquicias?retryWrites=true&w=majority"
}
