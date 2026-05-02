
# Utilisation d'une image légère Java 21
FROM eclipse-temurin:21-jdk-alpine

# Informations sur l'auteur / maintenance (optionnel mais recommandé)
LABEL maintainer="fozing@reservetable.cm"
LABEL description="Eureka Server - Service Discovery pour ReserveTable CM"

# Création d'un utilisateur non-root pour plus de sécurité
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Répertoire de travail dans le conteneur
WORKDIR /app

# Copie du fichier JAR généré par Maven
COPY target/*.jar app.jar

# Exposition du port sur lequel tourne Eureka
EXPOSE 8761

# Variables d'environnement
ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_OPTS=""

# Point d'entrée : commande pour lancer l'application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]