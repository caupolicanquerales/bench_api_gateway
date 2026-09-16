# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM to pre-fetch dependencies (layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy application source code and package executable jar
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy built artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Server port configuration (matches application.yml default 8082)
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
