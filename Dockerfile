# Use the official Temurin image with Java 21
FROM eclipse-temurin:21-jre

# Working directory inside the container
WORKDIR /app

# Copy the exact generated JAR
COPY target/franquicias-api-0.0.1-SNAPSHOT.jar app.jar

# Expose the port where the app runs
EXPOSE 8080

# Default command when starting the container
ENTRYPOINT ["java","-jar","/app/app.jar"]
