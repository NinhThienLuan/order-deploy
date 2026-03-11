# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom
COPY ../pom.xml ./pom.xml
COPY ../eureka-server/pom.xml ./eureka-server/pom.xml
COPY ../api-gateway/pom.xml ./api-gateway/pom.xml
COPY ../backend/pom.xml ./backend/pom.xml

# Copy service pom and source
COPY ../eureka-server/pom.xml ./eureka-server/
COPY ../eureka-server/src ./eureka-server/src

# Build the application
RUN mvn clean package -DskipTests -pl eureka-server -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for healthchecks
RUN apk --no-cache add curl

# Copy the built artifact
COPY --from=build /app/eureka-server/target/*.jar app.jar

# Expose the port
EXPOSE 8761

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
