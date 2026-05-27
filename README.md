# ziaant-backend-intergiciel

Backend microservices Ziaant avec Spring Boot, Docker Compose, PostgreSQL, Redis, MongoDB, RabbitMQ, Eureka et Gateway.

## Architecture Locale

Le fichier `docker-compose.yml` declare le projet Docker Desktop `ziaant-backend`.
Dans Docker Desktop, tous les conteneurs apparaissent donc dans un seul groupe/app.

Services principaux:

- `gateway-service`: point d'entree API
- `auth-service`: authentification, access token, refresh token, zero trust Redis
- `reservation-service`: reservations
- `restaurant-service`: restaurants, persistance SQL PostgreSQL
- `notification-service`: notifications, persistance MongoDB
- `eureka-server`: service discovery
- `postgresql`: base SQL
- `redis`: sessions refresh token et blacklist access token
- `mongodb`: stockage des notifications
- `rabbitmq`: broker des evenements notification

## Prerequis

- Java 21
- Maven
- Docker Desktop
- PowerShell

Verifications rapides:

```powershell
java -version
mvn -version
docker version
```

## Se Placer Dans Le Bon Dossier

Toutes les commandes Maven et Docker doivent etre lancees depuis le dossier du projet:

```powershell
cd "C:\Users\DIGITAL MARKET\WorkSpace\adrienne\ziaant-backend-intergiciel"
```

Verifie que tu es au bon endroit:

```powershell
dir pom.xml
```

Si `pom.xml` apparait, tu peux lancer Maven.

Erreur courante:

```text
The goal you specified requires a project to execute but there is no POM in this directory
```

Solution: revenir dans le dossier `ziaant-backend-intergiciel` avec la commande `cd` ci-dessus.

## Lancer Les Tests

Commande complete:

```powershell
mvn "-Dmaven.repo.local=C:\tmp\codex-m2-ziaant" test
```

Resultat attendu:

```text
BUILD SUCCESS
```

Cette commande lance les tests unitaires et les tests d'integration des services.

Tests couverts:

- Auth: login, access token 5 minutes, refresh token 7 jours
- Auth: rotation du refresh token
- Auth: logout et revocation de l'access token
- Auth: stockage zero trust via Redis
- Gateway: refus des tokens invalides ou revoques
- Gateway: comportement fail-closed si Redis est indisponible
- Notification: securite `X-Internal-Token`
- Notification: persistance MongoDB avec statut `SENT` ou `FAILED`
- Restaurant: service SQL via PostgreSQL/JPA
- Reservation, Eureka, Config, Notification: chargement de contexte et securite de base

## Lancer Tous Les Microservices Dans Docker Desktop

Premier lancement ou rebuild complet:

```powershell
.\start-ziaant.ps1 -Build
```

Relancer ensuite toute la pile:

```powershell
.\start-ziaant.ps1
```

Arreter toute la pile:

```powershell
.\stop-ziaant.ps1
```

Verifier les conteneurs:

```powershell
docker compose -p ziaant-backend ps
```

Voir les logs:

```powershell
docker compose -p ziaant-backend logs -f
```

## URLs Locales

- Gateway: `http://localhost:8090`
- Eureka: `http://localhost:8761`
- Auth service direct: `http://localhost:8081`
- Reservation service direct: `http://localhost:8082`
- Restaurant service direct: `http://localhost:8083`
- Notification service direct: `http://localhost:8084`
- RabbitMQ UI: `http://localhost:15672`
- Redis: `localhost:6380`
- MongoDB notification: `localhost:27018`
- PostgreSQL: `localhost:5433`

RabbitMQ UI:

```text
URL: http://localhost:15672
Username: ziaant
Password: ziaant123
```

## Configuration Des Tokens

- Access token: 5 minutes (`JWT_ACCESS_EXPIRATION=300000`)
- Refresh token: 7 jours (`JWT_REFRESH_EXPIRATION=604800000`)
- Le champ `token` n'est plus retourne apres login.
- Le frontend doit utiliser `accessToken`.
- Le refresh token est opaque, stocke en hash dans Redis, et tourne a chaque refresh.

Reponse login attendue:

```json
{
  "id": 2,
  "accessToken": "...",
  "refreshToken": "...",
  "accessTokenExpiresIn": 300,
  "refreshTokenExpiresIn": 604800,
  "name": "Client Test",
  "email": "client1@gmail.com",
  "phone": "699000000",
  "role": "CLIENT",
  "statut": "APPROUVE"
}
```

## Variables D'environnement

Pour personnaliser les secrets en local:

```powershell
Copy-Item .env.example .env
notepad .env
```

Docker Compose lit automatiquement le fichier `.env`.

Variables utiles:

```text
JWT_SECRET=change-me-in-production-change-me-in-production
JWT_ACCESS_EXPIRATION=300000
JWT_REFRESH_EXPIRATION=604800000
INTERNAL_SERVICE_TOKEN=change-me-in-production
SPRING_DATA_MONGODB_URI=mongodb://localhost:27018/notification_db
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
MAIL_USERNAME=
MAIL_PASSWORD=
```

## Tester Le Gateway

Health check:

```powershell
Invoke-RestMethod -Uri "http://localhost:8090/actuator/health"
```

## Tester Le Flow Auth

Creer un utilisateur:

```powershell
$email = "client$(Get-Date -Format yyyyMMddHHmmss)@gmail.com"
$password = "Password123!"
$registerBody = @{
  email = $email
  password = $password
  name = "Client Test"
  phone = "699000000"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8090/api/auth/register" `
  -ContentType "application/json" `
  -Body $registerBody
```

Se connecter:

```powershell
$loginBody = @{
  email = $email
  password = $password
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8090/api/auth/login" `
  -ContentType "application/json" `
  -Body $loginBody

$login
```

Verifier que `accessToken` et `refreshToken` sont presents:

```powershell
$login.accessToken
$login.refreshToken
$login.accessTokenExpiresIn
$login.refreshTokenExpiresIn
```

Tester le refresh:

```powershell
$oldRefreshToken = $login.refreshToken

$refreshBody = @{
  refreshToken = $oldRefreshToken
} | ConvertTo-Json

$refresh = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8090/api/auth/refresh" `
  -ContentType "application/json" `
  -Body $refreshBody

$refresh
```

Tester que l'ancien refresh token ne marche plus:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8090/api/auth/refresh" `
  -ContentType "application/json" `
  -Body $refreshBody
```

Resultat attendu: erreur `400 Bad Request`, car le refresh token a deja ete consomme.

Tester logout:

```powershell
$logoutBody = @{
  refreshToken = $refresh.refreshToken
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8090/api/auth/logout" `
  -Headers @{ Authorization = "Bearer $($refresh.accessToken)" } `
  -ContentType "application/json" `
  -Body $logoutBody
```

Verifier que le token est revoque:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8090/api/auth/validate?token=$($refresh.accessToken)"
```

Resultat attendu:

```json
{
  "valid": false
}
```

## Tester Redis

Ping Redis:

```powershell
docker exec -it ziaant-redis redis-cli ping
```

Resultat attendu:

```text
PONG
```

Voir les cles Redis:

```powershell
docker exec -it ziaant-redis redis-cli keys "*"
```

Les refresh tokens sont stockes sous:

```text
auth:refresh:...
```

Les access tokens revoques sont stockes sous:

```text
auth:blacklist:access:...
```

## Tester MongoDB Notification

Ouvrir le shell Mongo:

```powershell
docker exec -it ziaant-mongodb mongosh
```

Dans `mongosh`:

```javascript
show dbs
use notification_db
show collections
db.notifications.find().pretty()
```

Les notifications envoyees sont stockees dans:

```text
notification_db.notifications
```

Chaque document contient notamment:

```text
recipient
subject
body
status
errorMessage
createdAt
```

## Bases De Donnees Par Service

- `auth-service`: PostgreSQL pour les utilisateurs, Redis pour les sessions/tokens
- `restaurant-service`: PostgreSQL SQL/JPA
- `reservation-service`: PostgreSQL SQL/JPA
- `notification-service`: MongoDB pour l'historique des notifications

## Commandes Utiles Docker

Rebuild complet:

```powershell
docker compose -p ziaant-backend up -d --build
```

Arreter sans supprimer les volumes:

```powershell
docker compose -p ziaant-backend stop
```

Redemarrer:

```powershell
docker compose -p ziaant-backend start
```

Voir les logs d'un service:

```powershell
docker logs -f ziaant-auth
docker logs -f ziaant-gateway
docker logs -f ziaant-notification
```

