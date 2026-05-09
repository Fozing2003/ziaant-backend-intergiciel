FROM amazoncorretto:21-alpine
WORKDIR /app
COPY target/restaurant-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]