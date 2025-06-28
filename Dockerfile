FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# Instalar apenas wget (mais leve que curl e consistente com docker-compose)
RUN apt-get update && \
    apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*

# Criar volume para H2 database
VOLUME /app/data

# Health check usando wget (consistente com docker-compose)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Não definir JAVA_OPTS aqui - deixar para o docker-compose gerenciar
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]