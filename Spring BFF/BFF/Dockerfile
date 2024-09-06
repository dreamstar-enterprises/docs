# Use an official Java runtime as a parent image
FROM openjdk:17-jdk-alpine

# Create user to run the app as (instead of root)
RUN addgroup -S app && adduser -S app -G app

# User user "app"
USER app

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY build/libs/BFF-0.0.1-SNAPSHOT.jar /app/app.jar

# Set the command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Expose the port BFF will use
EXPOSE 9090

# accessible to all network interfaces (e.g. other containers) inside the docker network
CMD ["--server.address=0.0.0.0"]