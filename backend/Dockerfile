# syntax=docker/dockerfile:1
# Shared multi-stage build for every Spring Boot module. The builder stage is
# identical across services, so BuildKit builds the reactor once and reuses it.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
# JAR is the reactor-relative path to the repackaged artifact, e.g.
# services/user-service/target/user-service-1.0.0.jar
ARG JAR
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app app
COPY --from=build /workspace/${JAR} app.jar
# config-repo is only used by the config-server; harmless elsewhere.
COPY --from=build /workspace/config-repo /config-repo
USER app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
