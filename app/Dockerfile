# Multi-stage build for Spring Boot Kotlin application
FROM gradle:8.5-jdk17 AS builder

WORKDIR /build

# Copy Gradle files
COPY ../build.gradle.kts ../settings.gradle.kts ../gradle.properties ./
COPY ../gradle ./gradle

# Copy all module sources
COPY . ./app
COPY ../common ./common
COPY ../captcha ./captcha
COPY ../jwt-auth ./jwt-auth
COPY ../user ./user

# Build the application (skip tests for faster builds)
RUN gradle :app:bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/app/build/libs/*.jar app.jar

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose the application port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=docker"]

