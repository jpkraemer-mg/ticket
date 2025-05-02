FROM gradle:8.11.1-jdk21 AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src/ ./src

RUN gradle build --stacktrace --no-daemon -i

RUN rm /app/build/libs/*-plain.jar

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]