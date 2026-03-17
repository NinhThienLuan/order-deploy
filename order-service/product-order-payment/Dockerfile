# ========================================
# STANDALONE BUILD - Order Service Only
# ========================================

# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml first (for dependency caching)
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for healthchecks
RUN apk --no-cache add curl

# Copy the built artifact
COPY --from=build /app/target/*.jar app.jar

# Expose the port (default 8081, overridable)
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD sh -c "curl -f http://localhost:${PORT:-8081}/actuator/health || exit 1"

# Run the application with configurable profile
ENTRYPOINT ["sh", "-c", "java -jar -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-cloud} app.jar"]
