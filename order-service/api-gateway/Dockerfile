# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom and all module poms (for Maven reactor)
COPY pom.xml ./pom.xml
COPY eureka-server/pom.xml ./eureka-server/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY product-order-payment/pom.xml ./product-order-payment/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml

# Copy service source
COPY api-gateway/src ./api-gateway/src

# Build the application
RUN mvn clean package -DskipTests -e -pl api-gateway -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for healthchecks
RUN apk --no-cache add curl

# Copy the built artifact
COPY --from=build /app/api-gateway/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
