# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom and all module poms first (for Maven reactor)
COPY ../pom.xml ./pom.xml
COPY ../eureka-server/pom.xml ./eureka-server/pom.xml
COPY ../api-gateway/pom.xml ./api-gateway/pom.xml
COPY ../backend/pom.xml ./backend/pom.xml

# Copy backend source and build
COPY ../backend/src ./backend/src
RUN mvn package -pl backend -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for healthchecks
RUN apk --no-cache add curl

# Copy the built artifact
COPY --from=build /app/backend/target/*.jar app.jar

# Expose the port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
