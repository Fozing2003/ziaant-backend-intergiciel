# Étape 1 — Compiler le projet avec Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier pom.xml et télécharger les dépendances (mise en cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests -B

# Étape 2 — Image finale légère
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Métadonnées
LABEL maintainer="fozing@reservetable.cm"
LABEL description="User Service - Gestion des utilisateurs ReserveTable CM"

EXPOSE 8081

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]