# =============================
# 1. Build stage
# =============================
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and settings for dependency caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy the rest of the source code
COPY . .

# Build the application
RUN ./gradlew installDist

# =============================
# 2. Run stage
# =============================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy built application from build stage
COPY --from=build /app/build/install /app/install

# Expose the port your Ktor server listens on
EXPOSE 8080

# Run the application
CMD ["/app/install/sitEvent/bin/sitEvent"]
