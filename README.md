# ALERTMNS — Application de messagerie interne

**Projet 2024/2025 — Metz Numeric School**

## Stack technique
| Couche | Technologie |
|--------|------------|
| Frontend | Vue.js 3 + Vite + Pinia |
| Backend | Java 17 + Spring Boot 3.2 |
| Base de données | MySQL 8 |
| Temps réel | WebSocket (STOMP over SockJS) |
| Déploiement | Docker Compose |

---

## Lancement avec Docker Desktop

> Prérequis : **Docker Desktop** installé et lancé.

```bash
# 1. Cloner / se placer dans le dossier
cd /chemin/vers/ALERTMNS

# 2. Lancer toute la stack (première fois ~3-5 min pour build)
docker compose up --build

# 3. L'application est disponible sur :
#    http://localhost       ← Interface web
#    http://localhost:8080  ← API Spring Boot
#    http://localhost:8081  ← Adminer (interface base de données)
#    localhost:3306         ← MySQL
```

Pour arrêter :
```bash
docker compose down
```

Pour reset complet (données incluses) :
```bash
docker compose down -v
```

---

## Compte par défaut

| Champ | Valeur |
|-------|--------|
| Email | `admin@alertmns.fr` |
| Mot de passe | `Admin1234!` |
| Rôle | Admin |

---

## Fonctionnalités implémentées

### Messagerie
- ✅ Canaux publics et privés (#général, #dev, etc.)
- ✅ Discussions en temps réel (WebSocket)
- ✅ Pièces jointes (upload fichiers)
- ✅ Messages non lus avec badge
- ✅ Export conversation (JSON / CSV / XML)

### Gestion des utilisateurs
- ✅ Inscription / Connexion (JWT)
- ✅ Rôles : Admin, RH, Responsable, Collaborateur
- ✅ Activation / Désactivation de compte
- ✅ Message d'absence configurable
- ✅ Affectation à une structure (service / agence)

### Pointage & Présence
- ✅ Enregistrement arrivée / départ
- ✅ Calcul automatique journalier / hebdomadaire / mensuel
- ✅ Liste des personnes présentes en temps réel

### Réunions
- ✅ Création de réunion avec date et participants
- ✅ Invitation par notification
- ✅ Réponse : accepter / refuser

### Notifications
- ✅ Notifications temps réel (WebSocket)
- ✅ Badge non lu
- ✅ Marquage lu / tout marquer lu

### Administration
- ✅ Gestion des utilisateurs (Admin / RH)
- ✅ Gestion des canaux
- ✅ Gestion des structures

---

## Architecture

```
ALERTMNS/
├── docker-compose.yml          ← Orchestration des services
├── alertmns_db.sql             ← Schéma + données de test
├── backend/                    ← Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/alertmns/
│       ├── entity/             ← 14 entités JPA
│       ├── repository/         ← 11 repositories
│       ├── service/            ← 8 services
│       ├── controller/         ← 9 controllers REST + WS
│       ├── security/           ← JWT + Spring Security
│       ├── config/             ← WebSocket, CORS, Security
│       └── dto/                ← Data Transfer Objects
└── frontend/                   ← Vue.js 3
    ├── Dockerfile
    ├── nginx.conf
    └── src/
        ├── views/              ← 7 vues principales
        ├── components/         ← Sidebar, Layout, Notifications
        ├── stores/             ← Pinia (auth, channels, messages, notifs)
        ├── composables/        ← WebSocket, dates
        └── api/                ← Axios instance
```

---

## Développement local (sans Docker)

### Backend
```bash
cd backend
# Modifier application.properties : spring.datasource.url=jdbc:mysql://localhost:3306/alertmns
./mvn-local.sh spring-boot:run
```

Si `mvn` utilise un JDK trop recent sur ta machine, tu peux aussi lancer ponctuellement :

```bash
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn -DskipTests package
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Disponible sur http://localhost:5173
```
