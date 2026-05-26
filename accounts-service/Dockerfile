# ================================================================
# accounts-service Dockerfile — Multi-stage build
# Stage 1: build com Maven
# Stage 2: runtime com JRE mínimo (distroless)
# ================================================================

# ── Stage 1: Build ───────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copia POMs primeiro para cache de dependências
COPY pom.xml .
COPY common/pom.xml common/
COPY accounts-service/pom.xml accounts-service/
COPY transfers-service/pom.xml transfers-service/
COPY boletos-service/pom.xml boletos-service/
COPY pix-service/pom.xml pix-service/
COPY api-gateway/pom.xml api-gateway/

# Download de dependências (cacheia se POMs não mudarem)
RUN apk add --no-cache maven && \
    mvn -B dependency:go-offline -pl accounts-service -am --no-transfer-progress

# Copia o source
COPY common/src common/src
COPY accounts-service/src accounts-service/src

# Build (codegen incluso na fase generate-sources)
RUN mvn -B clean package -pl accounts-service -am -DskipTests --no-transfer-progress

# ── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S banking && adduser -S banking -G banking
USER banking

# Copia apenas o JAR
COPY --from=builder /workspace/accounts-service/target/accounts-service-*.jar app.jar

# Expõe porta e configura JVM para containers
EXPOSE 8081

ENTRYPOINT ["java", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]