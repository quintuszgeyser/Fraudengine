
# Dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the Spring Boot fat jar (ensure this filename matches your build)
COPY target/fraudengine-0.0.1-SNAPSHOT.jar app.jar

# Default profile & optional JVM opts
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

# Expose REST + ISO8583 ports (if your app listens on 8037)
EXPOSE 8080 8037

# Exec-form entrypoint; supports JAVA_OPTS via shell
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE"]
