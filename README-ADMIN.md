# Créer un compte Admin

## Étape 1 — Lancer les services
```bash
docker compose up -d
```

## Étape 2 — Créer le compte admin
```bash
curl -s -X POST http://localhost:8090/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin Principal",
    "email": "admin@gmail.com",
    "password": "admin123",
    "phone": "699000000",
    "adminSecret": "ZIAANT_ADMIN_SECRET_2025"
  }'
```

## Identifiants
- Email    : admin@gmail.com
- Password : admin123
- Secret   : ZIAANT_ADMIN_SECRET_2025
