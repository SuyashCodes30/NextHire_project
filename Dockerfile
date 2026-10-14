# ==============================================================================
# NextHire AI Platform — Root Dockerfile for Render Web Service
# Used when Render builds from repository root
# ==============================================================================

# Stage 1: Build the backend application
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

COPY backend/pom.xml .
RUN mvn dependency:go-offline -B || true

COPY backend/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight JRE 21 Runtime Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-privileged user for security
RUN addgroup -S nexthire && adduser -S nexthire -G nexthire
USER nexthire

COPY --from=builder /workspace/target/*.jar app.jar

ENV PORT=8080
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar"]
