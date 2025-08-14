# =============================
# 1. Build stage
# =============================
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy wrapper & gradle files first to improve Docker cache usage
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy project sources
COPY . .

# Ensure gradlew is executable and remove CRLF (Windows line endings) if present,
# then build the distribution
RUN chmod +x gradlew \
    && sed -i 's/\r$//' gradlew || true \
    && ./gradlew installDist --no-daemon

# =============================
# 2. Run stage
# =============================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy built application from build stage (install directory)
COPY --from=build /app/build/install /app/install

# Expose the port your Ktor server listens on
EXPOSE 8080

# Adjust application name if different in settings.gradle.kts
CMD ["/app/install/sitevent/bin/sitevent"]
