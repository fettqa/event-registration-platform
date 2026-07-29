# Multi-stage build for Event Registration Platform (Spring Boot 21)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY app/gradlew app/gradlew.bat ./
COPY app/gradle ./gradle
COPY app/build.gradle app/settings.gradle ./
COPY app/src ./src

RUN chmod +x gradlew \
  && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/build/libs/*SNAPSHOT.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
