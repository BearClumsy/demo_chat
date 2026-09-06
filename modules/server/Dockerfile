# Build context is the repository root, not modules/server, because the Gradle wrapper and
# settings.gradle live there:
#   docker build -f modules/server/Dockerfile -t demo-chat-server .

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

# Wrapper and build scripts first, so a source-only change doesn't re-resolve dependencies.
COPY gradlew settings.gradle ./
COPY gradle gradle
COPY modules/server/build.gradle modules/server/
COPY modules/client/build.gradle modules/client/
RUN ./gradlew --no-daemon :server:dependencies --configuration runtimeClasspath > /dev/null

COPY modules/server/src modules/server/src
# Tests need Docker (Testcontainers) and run in CI, not in the image build.
RUN ./gradlew --no-daemon :server:bootJar -x test \
    && cp modules/server/build/libs/*.jar /workspace/application.jar

# Split the jar into layers that change at different rates, so a code-only rebuild reuses the
# dependency layers below.
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre-alpine AS runtime
# Pinned uid so a PodSecurity "restricted" namespace can assert runAsNonRoot / runAsUser.
RUN addgroup -S -g 10001 demo_chat && adduser -S -u 10001 -G demo_chat demo_chat
WORKDIR /app

COPY --from=build --chown=demo_chat:demo_chat /workspace/extracted/dependencies/ ./
COPY --from=build --chown=demo_chat:demo_chat /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=demo_chat:demo_chat /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=demo_chat:demo_chat /workspace/extracted/application/ ./

USER demo_chat
EXPOSE 8080

# SPRING_PROFILES_ACTIVE is deliberately unset: the orchestrator (docker run / ECS task definition)
# picks staging or prod, and the app falls back to `local` only if nothing sets it.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "application.jar"]
