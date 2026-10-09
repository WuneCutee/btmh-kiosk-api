# Multi-stage build cho kiosk-api (BTMH Kiosk API - Phase 1)
# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn

RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd -m -u 1000 appuser

COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appuser app.jar
RUN mkdir -p /app/logs && chown appuser:appuser /app/logs

USER appuser

# Cong mac dinh 8091 - khong trung voi webview (8080) hay kiosk-admin (8090).
EXPOSE 8091

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT:-8091}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
