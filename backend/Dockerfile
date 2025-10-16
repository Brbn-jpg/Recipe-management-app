# =================================================================
# STAGE 1: Building the application (Builder)
# =================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package 

# =================================================================
# STAGE 2: Runtime Environment (Runtime)
# =================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]
