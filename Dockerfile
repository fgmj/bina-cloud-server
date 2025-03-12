# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Add health check dependencies
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Copy application
COPY --from=builder /build/target/bina-cloud-server-0.0.1-SNAPSHOT.jar app.jar

# Create volume for H2 database
VOLUME /app/data

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Use H2 by default, switch to Oracle profile if USE_ORACLE is set
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar ${USE_ORACLE:+--spring.profiles.active=oracle}"] 