# ALERTMNS — Application de messagerie interne

**Projet 2024/2025 — Metz Numeric School**

## Stack technique

| Couche | Technologie |
|--------|------------|
| Frontend | Vue.js 3 + Vite + Pinia |
| Backend | Java 17 + Spring Boot 3.2 |
| Base de données | MySQL 8 |
| Temps réel | WebSocket STOMP over SockJS |
| Déploiement | Docker Compose + Nginx |
| Tests | JUnit 5 / Mockito (backend) · Vitest (frontend) |

---

## Démarrage rapide (Docker Desktop)

> **Prérequis :** Docker Desktop installé et lancé.

```bash
# 1. Cloner et se placer dans le dossier
git clone https://github.com/GauthierDEBRA/ALERTMNS.git
cd ALERTMNS

# 2. Préparer les variables d'environnement
cp .env.example .env
# Ouvrir .env et remplacer JWT_SECRET par une vraie valeur aléatoire :
# openssl rand -hex 32

# 3. Lancer toute la stack (première fois ~3–5 min pour le build)
docker compose up --build

# 4. L'application est disponible sur :
#    http://localhost       ← Interface web
#    http://localhost:8080  ← API Spring Boot
#    http://localhost:8081  ← Adminer (interface base de données)
#    http://localhost:8025  ← Mailpit (visualiser les emails de dev)
#    localhost:3306         ← MySQL
```

**Arrêter :**
```bash
docker compose down
```

**Reset complet (supprime les données) :**
```bash
docker compose down -v
```

---

## Compte administrateur par défaut

| Champ | Valeur |
|-------|--------|
| Email | `admin@alertmns.fr` |
| Mot de passe | `Admin1234!` |
| Rôle | Admin |

---

## Fonctionnalités

### Messagerie
- ✅ Canaux publics et conversations privées
- ✅ Messages en temps réel (WebSocket STOMP)
- ✅ Pagination infinie par curseur (scroll vers le haut)
- ✅ Indicateur de frappe en temps réel
- ✅ Réactions emoji sur les messages
- ✅ Pièces jointes (upload, max 10 Mo)
- ✅ Modification / suppression de messages
- ✅ Recherche dans les conversations
- ✅ Export conversation (JSON / CSV / XML)

### Notifications
- ✅ Notifications temps réel (WebSocket user-queue)
- ✅ Deep links — clic → canal exact ou réunion exacte
- ✅ Préférences par type (messages, réunions, absences)
- ✅ Fallback polling si WebSocket indisponible

### Gestion des utilisateurs
- ✅ Inscription / Connexion (JWT + refresh token httpOnly)
- ✅ Rôles : Admin, RH, Responsable, Collaborateur
- ✅ Activation / désactivation de compte
- ✅ Message d'absence configurable
- ✅ Photo de profil (upload)
- ✅ Affectation à une structure (service / agence)

### Pointage & Présence
- ✅ Enregistrement arrivée / départ
- ✅ Durée journalière / hebdomadaire / mensuelle
- ✅ Liste des personnes présentes en temps réel

### Réunions
- ✅ Création avec participants, lieu, lien visio
- ✅ Invitation par notification avec deep link
- ✅ Réponse : accepter / refuser
- ✅ Rappels automatiques (J-1, H-1, 30 min avant)
- ✅ Détection de conflits d'agenda

### Administration
- ✅ Gestion des utilisateurs (Admin / RH)
- ✅ Gestion des canaux et de leurs membres
- ✅ Gestion des structures

---

## Tests

### Backend (JUnit 5 + Spring Boot Test)
```bash
cd backend
bash mvn-local.sh test
```

| Classe | Couverture |
|--------|-----------|
| `JwtUtilTest` | Génération, extraction, validation, token falsifié |
| `MessageRepositoryTest` | Pagination curseur (findLatest, findBefore) |
| `NotificationServiceTest` | Persistance, préférences utilisateur par type |
| `RefreshTokenServiceTest` | Émission, consommation, révocation |
| `PointageRepositoryTest` | Pointages chevauchant minuit |

### Frontend (Vitest)
```bash
cd frontend
npm test
```

| Fichier | Couverture |
|---------|-----------|
| `avatar.test.js` | `getAvatarInitials`, `getAvatarColor` |
| `useToast.test.js` | Cycle de vie toast, auto-dismiss |
| `messages.store.test.js` | addMessage, déduplication temp-id, unread, clearChannel |

---

## Architecture

```
ALERTMNS/
├── docker-compose.yml          ← Orchestration (db, backend, frontend, mailpit, adminer)
├── .env.example                ← Variables d'environnement à copier dans .env
├── alertmns_db.sql             ← Schéma MySQL + données initiales
├── backend/                    ← Spring Boot 3.2 / Java 17
│   ├── Dockerfile              ← Multi-stage Maven → JRE-jammy
│   ├── src/main/java/com/alertmns/
│   │   ├── entity/             ← Entités JPA (Message, Reunion, Utilisateur…)
│   │   ├── repository/         ← Spring Data JPA + requêtes JPQL paginées
│   │   ├── service/            ← Logique métier (MessageService, ReunionService…)
│   │   ├── controller/         ← Controllers REST + WebSocketController
│   │   ├── security/           ← JWT, Spring Security, STOMP auth interceptor
│   │   ├── config/             ← WebSocket, CORS, Security config
│   │   └── dto/                ← Data Transfer Objects
│   └── src/test/               ← Tests JUnit 5 (@DataJpaTest, @SpringBootTest)
└── frontend/                   ← Vue.js 3 + Vite
    ├── Dockerfile              ← Multi-stage Node → Nginx alpine
    ├── nginx.conf              ← SPA fallback, proxy API/WS, sécurité HTTP
    └── src/
        ├── views/              ← ChatView, ReunionsView, PointageView, AdminView…
        ├── components/         ← AppLayout, Sidebar, NotificationPanel…
        ├── stores/             ← Pinia (auth, channels, messages, notifications)
        ├── composables/        ← useWebSocket, useToast, useDarkMode, useDate
        ├── utils/              ← avatar.js
        └── api/                ← Axios + intercepteur refresh token
```

### Flux d'authentification

```
Login → JWT (15 min) + Refresh token httpOnly cookie (30 j)
       ↓
Axios intercepteur : 401 → POST /auth/refresh → nouveau JWT
       ↓
WebSocket : token transmis dans connectHeaders STOMP CONNECT
             (jamais dans l'URL — logs serveur, Referer)
       ↓
Session expirée : logout propre + message affiché sur /login
```

---

## Développement local (sans Docker)

### Prérequis
- Java 17+, Maven 3.9+
- Node.js 20+, npm
- MySQL 8 (local ou Docker)

### Backend
```bash
cd backend

# Lancer MySQL séparément si besoin :
# docker run -d -p 3306:3306 -e MYSQL_DATABASE=alertmns \
#   -e MYSQL_USER=alertmns -e MYSQL_PASSWORD=alertmns \
#   -e MYSQL_ROOT_PASSWORD=root mysql:8.0

export JWT_SECRET='une-cle-locale-longue-dau-moins-32-caracteres!!'
bash mvn-local.sh spring-boot:run
# API disponible sur http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Interface disponible sur http://localhost:5173
# (proxy /api et /ws vers localhost:8080 configuré dans vite.config.js)
```

---

## Variables d'environnement

| Variable | Obligatoire | Description |
|----------|-------------|-------------|
| `JWT_SECRET` | ✅ | Clé HMAC ≥ 32 caractères (`openssl rand -hex 32`) |
| `MYSQL_ROOT_PASSWORD` | ✅ | Mot de passe root MySQL |
| `MYSQL_DATABASE` | ✅ | Nom de la base (défaut : `alertmns`) |
| `MYSQL_USER` | ✅ | Utilisateur MySQL |
| `MYSQL_PASSWORD` | ✅ | Mot de passe MySQL |
| `ALERTMNS_APP_BASE_URL` | — | URL publique pour les liens dans les emails |
| `ALERTMNS_MAIL_FROM` | — | Adresse expéditeur des emails |
| `SPRING_MAIL_HOST` | — | Hôte SMTP (défaut : mailpit en dev) |
| `SPRING_MAIL_PORT` | — | Port SMTP (défaut : 1025) |
| `SPRING_MAIL_USERNAME` | — | Login SMTP (prod uniquement) |
| `SPRING_MAIL_PASSWORD` | — | Mot de passe SMTP (prod uniquement) |

Voir `.env.example` pour le fichier complet.
