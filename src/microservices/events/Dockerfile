# Stage 1: Build the application
FROM eclipse-temurin:21 AS builder

# Set working directory
WORKDIR /app

# Copy the Maven wrapper files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Install curl for retry logic
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Function to retry Maven commands with exponential backoff and fallback to standard Maven
RUN echo '#!/bin/bash' > /usr/local/bin/mvnw-retry.sh && \
    echo 'set -e' >> /usr/local/bin/mvnw-retry.sh && \
    echo 'MAX_RETRIES=3' >> /usr/local/bin/mvnw-retry.sh && \
    echo 'RETRY_DELAY=5' >> /usr/local/bin/mvnw-retry.sh && \
    echo 'for i in $(seq 1 $MAX_RETRIES); do' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  if ./mvnw "$@"; then' >> /usr/local/bin/mvnw-retry.sh && \
    echo '    exit 0' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  fi' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  if [ $i -lt $MAX_RETRIES ]; then' >> /usr/local/bin/mvnw-retry.sh && \
    echo '    echo "Attempt $i failed, retrying in ${RETRY_DELAY}s..."' >> /usr/local/bin/mvnw-retry.sh && \
    echo '    sleep $RETRY_DELAY' >> /usr/local/bin/mvnw-retry.sh && \
    echo '    RETRY_DELAY=$((RETRY_DELAY * 2))' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  fi' >> /usr/local/bin/mvnw-retry.sh && \
    echo 'done' >> /usr/local/bin/mvnw-retry.sh && \
    echo 'echo "Maven wrapper failed, trying standard Maven..."' >> /usr/local/bin/mvnw-retry.sh && \
    echo 'if command -v mvn >/dev/null 2>&1; then' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  echo "Using standard Maven installation"' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  mvn "$@"' >> /usr/local/bin/mvnw-retry.sh && \
    echo 'else' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  echo "Installing Maven..."' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*' >> /usr/local/bin/mvnw-retry.sh && \
    echo '  mvn "$@"' >> /usr/local/bin/mvnw-retry.sh && \
    echo 'fi' >> /usr/local/bin/mvnw-retry.sh && \
    chmod +x /usr/local/bin/mvnw-retry.sh

# Download dependencies and cache them (will be cached by Docker if pom.xml doesn't change)
# Use retry mechanism to handle network issues
RUN --mount=type=cache,target=/root/.m2 \
    /usr/local/bin/mvnw-retry.sh dependency:go-offline -B

# Copy source code after dependencies to leverage build caching
COPY src/ ./src/

# Build the application
RUN --mount=type=cache,target=/root/.m2 \
    /usr/local/bin/mvnw-retry.sh package -DskipTests -B

# Stage 2: Create runtime image
FROM eclipse-temurin:21-alpine

# Add a non-root user to run the app
RUN addgroup -S javauser && \
    adduser -S -G javauser -u 1001 javauser

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/events-service-1.0.0.jar app.jar

# Set ownership of the application files to non-root user
RUN chown -R javauser:javauser /app

# Switch to non-root user
USER javauser

# Expose application port
EXPOSE 8082

# Configure JVM options (optimized for containers)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/api/events/health || exit 1

# Run the application
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]
