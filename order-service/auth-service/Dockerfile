# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom and all module poms first (for Maven reactor)
COPY ../pom.xml ./pom.xml
COPY ../eureka-server/pom.xml ./eureka-server/pom.xml
COPY ../api-gateway/pom.xml ./api-gateway/pom.xml
COPY ../product-order-payment/pom.xml ./product-order-payment/pom.xml
COPY ../auth-service/pom.xml ./auth-service/pom.xml

# Copy auth-service source and build
COPY ../auth-service/src ./auth-service/src
RUN mvn package -pl auth-service -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for healthchecks
RUN apk --no-cache add curl

# Copy the built artifact
COPY --from=build /app/auth-service/target/*.jar app.jar

# Expose the port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
