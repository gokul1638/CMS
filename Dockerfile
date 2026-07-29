# ==========================================
# Phase 1: Build Java Package JAR
# ==========================================
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source tree and compile
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Phase 2: Lightweight JVM Runner Image
# ==========================================
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the compiled JAR from Phase 1
COPY --from=builder /app/target/cloud-management-system-1.0.0.jar app.jar

# Open standard backend ports
EXPOSE 8081

# Command execution entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
