# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy maven wrapper and pom.xml first to leverage Docker cache
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies using a Docker cache mount for the Maven local repository.
# This prevents downloading all dependencies from the internet on every cache invalidation.
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline

# Copy source and build
COPY src ./src
# Build and package using the same Docker cache mount to leverage cached JAR files.
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
