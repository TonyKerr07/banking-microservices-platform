FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/
COPY accounts-service/pom.xml accounts-service/
COPY transfers-service/pom.xml transfers-service/
COPY boletos-service/pom.xml boletos-service/
COPY pix-service/pom.xml pix-service/
COPY api-gateway/pom.xml api-gateway/

RUN apk add --no-cache maven && \
    mvn -B dependency:go-offline -pl api-gateway -am --no-transfer-progress

COPY api-gateway/src api-gateway/src

RUN mvn -B clean package -pl api-gateway -am -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S banking && adduser -S banking -G banking
USER banking
COPY --from=builder /workspace/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", \
  "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]