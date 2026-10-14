# ==============================================================================
# NextHire AI Platform — Spring Boot Backend Dockerfile
# Optimized for Render Web Service (Free Tier)
# ==============================================================================

# Stage 1: Build the Spring Boot application using Maven & Java 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy POM and fetch dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build runnable fat JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight JRE 21 Runtime Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-privileged user for security
RUN addgroup -S nexthire && adduser -S nexthire -G nexthire
USER nexthire

# Copy built jar from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Render assigns port dynamically via $PORT (defaults to 8080)
ENV PORT=8080
# Optimize memory limits for Render 512MB free tier container
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar"]
