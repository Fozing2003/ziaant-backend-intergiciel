FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw package -DskipTests
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "target/auth-service-0.0.1-SNAPSHOT.jar"]
