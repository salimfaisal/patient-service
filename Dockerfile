# Build stage
# Uses an image containing Ubuntu Jammy plus Java 21’s JDK.
FROM eclipse-temurin:21-jdk-jammy AS build
LABEL authors="fasalim"
# Sets /workspace as the current directory inside the build container. Future commands run here.
WORKDIR /workspace
# Copies the Maven Wrapper files and pom.xml first.
# Docker caches each layer. If only Java source changes, Docker can reuse already downloaded Maven dependencies
COPY .mvn .mvn
COPY mvnw pom.xml ./
# -B means non-interactive mode, suitable for builds.
# dependency:go-offline downloads dependencies before copying source code.
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
# Copies application source code into the container.
COPY src src
# Builds the Spring Boot JAR.
RUN ./mvnw -B clean package -DskipTests


# Runtime stage
# Starts a new, smaller image containing only the Java JRE, not Maven or the Java compiler.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Creates a non-root Linux user named spring.
# Running applications as root inside containers is unsafe; this reduces the blast radius if the app is compromised.
RUN useradd --system --create-home spring
# Copies only the built JAR from the earlier stage named build.
COPY --from=build /workspace/target/patient-0.0.1-SNAPSHOT.jar app.jar
# All following commands, including the app process, run as the non-root spring user.
USER spring
# Documents that the container listens on port 8080
EXPOSE 8080
# The command Docker runs when the container starts:
ENTRYPOINT ["java", "-jar", "/app/app.jar"]