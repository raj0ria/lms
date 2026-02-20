# -------- Stage 1: Build --------
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app
COPY . .

RUN gradle clean build -x test

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Create data directory for H2
RUN mkdir -p /app/data

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
