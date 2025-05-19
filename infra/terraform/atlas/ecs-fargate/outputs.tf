output "alb_dns_name" {
  description = "URL pública del ALB"
  value       = aws_lb.app.dns_name
}
