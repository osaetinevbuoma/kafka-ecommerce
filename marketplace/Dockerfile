# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradlew ./
COPY gradlew.bat ./
COPY build.gradle ./

COPY gradle ./gradle
COPY src ./src

RUN ["./gradlew", "clean"]
CMD ["./gradlew", "bootRun"]
