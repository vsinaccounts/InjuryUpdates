# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
CMD ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "target/injury-feed-service-1.0.0.jar"] 