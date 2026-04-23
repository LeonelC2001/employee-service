# ─────────────────────────────────────────────
# Stage 1 — Build
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven wrapper and pom first (layer-cache friendly)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B -q

# Copy source and build (skip tests — tests run in CI pipeline)
COPY src ./src
RUN ./mvnw package -DskipTests -B -q

# Extract Spring Boot layers for optimized caching
RUN java -Djarmode=layertools \
         -jar target/employee-service-*.jar extract \
         --destination target/extracted

# ─────────────────────────────────────────────
# Stage 2 — Runtime (minimal JRE image)
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy Spring Boot layers (most stable → least stable)
COPY --from=builder /workspace/target/extracted/dependencies          ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader    ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies ./
COPY --from=builder /workspace/target/extracted/application           ./

# Log directory
RUN mkdir -p /var/log/employee-service && \
    chown -R appuser:appgroup /var/log/employee-service /app

USER appuser

EXPOSE 8080

# Health check (used by Docker Compose and Kubernetes liveness probe)
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.JarLauncher"]
