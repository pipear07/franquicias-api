output "vpc_id" {
  description = "ID de la VPC existente"
  value       = data.aws_vpc.existing.id
}

output "public_subnet_ids" {
  description = "IDs de las subnets públicas"
  value       = [
    data.aws_subnet.public_a.id,
    data.aws_subnet.public_b.id,
    data.aws_subnet.public_c.id,
  ]
}
