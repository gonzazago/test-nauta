FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle/ gradle/

RUN chmod +x ./gradlew

RUN ./gradlew dependencies --build-cache

COPY . .

RUN ./gradlew :orchestator-app:shadowJar --no-daemon --parallel --build-cache --stacktrace

FROM eclipse-temurin:17-jre-jammy

ARG APP_JAR_FILE=/app/orchestator-app-fat.jar

ENV JVM_ARGS="-XX:+UseContainerSupport -XX:InitialRAMPercentage=60 -XX:MaxRAMPercentage=60 -XX:MaxDirectMemorySize=200m --add-opens=java.base/java.net=ALL-UNNAMED"
ENV APP_JAR_FILE=${APP_JAR_FILE}

WORKDIR /app

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

COPY --from=builder /app/orchestator-app/build/libs/*-fat.jar ${APP_JAR_FILE}

RUN chown -R appuser:appgroup /app

USER appuser

ENTRYPOINT ["sh", "-c"]
CMD ["exec java ${JVM_ARGS} -jar ${APP_JAR_FILE}"]
EXPOSE 8080
