# Multi-stage build for production
FROM openjdk:17-jdk-slim AS build

# Install Maven manually
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.4/binaries/apache-maven-3.9.4-bin.tar.gz && \
    tar -xzf apache-maven-3.9.4-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.4 /opt/maven && \
    rm apache-maven-3.9.4-bin.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set Maven environment
ENV MAVEN_HOME=/opt/maven
ENV PATH=${MAVEN_HOME}/bin:${PATH}

# Set working directory
WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM openjdk:17-jdk-alpine

# Install wget for health checks and add a non-root user for security
RUN apk add --no-cache wget && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/injury-feed-service-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/injury-updates/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 