# Template de projet REST API avec Javalin - IFT2255

Ce dépôt sert de template de base pour les projets REST API avec Javalin dans le cadre du cours IFT2255 – Génie logiciel.
Il fournit une structure organisée suivant une architecture MVC (Model–View–Controller) simplifiée, prête à être utilisée pour vos travaux.

## Structure du projet

```sh
rest-api/
│
├── src/
│   ├── main/
│   │   ├── java/com/diro/ift2255/
│   │   │   ├── config/
│   │   │   │   └── Routes.java           # Définition des routes HTTP
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── CourseController.java # Endpoints liés aux cours
│   │   │   │   ├── UserController.java   # Endpoints utilisateurs
│   │   │   │   ├── ReviewController.java # Avis étudiants
│   │   │   │   ├── ProgramController.java # Programmes académiques
│   │   │   │   └── CourseSetController.java # Ensembles de cours
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Course.java
│   │   │   │   ├── User.java
│   │   │   │   ├── Review.java
│   │   │   │   ├── Program.java
│   │   │   │   ├── CourseSet.java
│   │   │   │   └── AcademicResult.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── CourseService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ReviewService.java
│   │   │   │   ├── ProgramService.java
│   │   │   │   ├── CourseSetService.java
│   │   │   │   └── AcademicResultService.java
│   │   │   │
│   │   │   ├── util/
│   │   │   │   ├── HttpClientAPI.java
│   │   │   │   ├── HttpResponse.java
│   │   │   │   ├── HttpStatus.java
│   │   │   │   ├── ResponseUtil.java
│   │   │   │   └── ValidationUtil.java
│   │   │   │
│   │   │   ├── cli/
│   │   │   │   └── CliApp.java           # Point d'entrée du client CLI
│   │   │   │
│   │   │   └── Main.java                 # Point d'entrée du serveur Javalin
│   │   │
│   │   └── resources/                    # Ressources (CSV, config, etc.)
│   │
│   └── test/                             # Tests unitaires JUnit
│
├── discord-bot/                          # Bot Discord pour avis étudiants
│   └── README.md
│
└── pom.xml
```

## Architecture

Le projet suit principalement le modèle MVC :

**Model (model/)**

Représentation des entités du domaine (cours, utilisateurs, avis, ensembles).

**Controller (controller/)**

Gestion des requêtes HTTP et validation des entrées.

**Service (service/)**

Logique métier centrale (règles, agrégation, appels externes).

**Util (util/)**

Fonctions utilitaires partagées (validation, réponses HTTP, client REST).

**Config (config/)**

Centralisation des routes REST.

**CLI (cli/)**

Client en ligne de commande consommant l'API REST.

**Main.java**

Point d'entrée du serveur REST (initialisation de Javalin).

## Bonnes pratiques appliquées

- Séparation claire des responsabilités (Controller / Service / Model).
- Centralisation des routes REST dans \`Routes.java\`.
- Utilisation de services testables indépendamment de Javalin.
- Tests unitaires JUnit pour les services.
- Dépendances isolées via Maven.
- Client CLI découplé du serveur REST.

## Prérequis

- **Java 17**
- **Maven 3.x**

## Vérifier les installations

```bash
java -version
mvn -v
```

## Installation

**Cloner le dépôt et se placer dans le module REST**

```bash
git clone https://github.com/FaustGzx/ift2255-devoir2.git
cd ift2255-devoir2/rest-api
```

**Construire le projet et générer les JARs**

```bash
mvn -DskipTests package
```

Cela génère dans `target/` :

- `ift2255-cli.jar` → fat-jar exécutable du client CLI
- `rest-api-1.0-SNAPSHOT.jar` → JAR standard contenant les classes du serveur REST (non exécutable).  
  Le serveur se lance via Maven avec : `mvn exec:java@server`

**Build complet recommandé (pour validation complète)**

Pour exécuter les tests ET générer les JARs en une seule commande :

```bash
mvn clean test
```

Ou pour un build complet avec packaging :

```bash
mvn clean package
```

Pour un build rapide sans tests (développement) :

```bash
mvn -DskipTests clean package
```

## Exécution de l'application

**IMPORTANT:** L'API et la CLI doivent être lancées dans deux terminaux distincts.

### 1. Lancer le serveur REST (API Javalin)

```bash
cd ift2255-devoir2/rest-api
mvn exec:java@server
```

Serveur disponible à : **http://localhost:7070**

### 2. Lancer la CLI (méthode recommandée)

Dans un second terminal :

```bash
cd ift2255-devoir2/rest-api
java -jar target/ift2255-cli.jar
```

### 3. Lancer la CLI via Maven (optionnel)

```bash
cd ift2255-devoir2/rest-api
mvn exec:java@cli
```

### 4. Configurer l'URL de l'API (optionnel)

Par défaut, la CLI utilise \`http://localhost:7070\`.

**Via variable d'environnement**

```bash
API_BASE_URL=http://localhost:7070 java -jar target/ift2255-cli.jar
```

**Via Maven**

```bash
mvn exec:java@cli -Dexec.args="--baseUrl=http://localhost:7070"
```



## Fonctionnalités par rôle

### Utilisateur (Étudiant)

L’étudiant utilise CLI pour interagir avec l’outil.
Il peut :

- Rechercher des cours par sigle, préfixe, nom ou description

- Consulter les détails complets d’un cours (crédits, prérequis, sessions offertes)

- Afficher l’horaire d’un cours pour un trimestre donné

- Vérifier son éligibilité à un cours en fonction des prérequis et du cycle d’études

- Consulter les résultats académiques agrégés d’un cours (score, moyenne, participants)

- Comparer plusieurs cours afin d’estimer la difficulté et la charge de travail

- Créer des ensembles de cours représentant des chemins de session possibles

- Visualiser l’horaire résultant d’un ensemble de cours

- Détecter les conflits d’horaire dans un ensemble de cours (bonus)

- Comparer deux ensembles de cours afin d’évaluer différents scénarios de session

### Bot Discord (Collecte d’avis)

Le bot Discord est un point d’entrée pour la collecte des avis étudiants.
Il permet :

- De recueillir des avis étudiants via un canal Discord dédié

- D’extraire automatiquement la difficulté, la charge de travail et les commentaires

- De transmettre ces avis à l’API REST via l’endpoint POST /avis

- Enrichir les données de comparaison des cours à partir d’expériences réelles d’étudiants

### API REST (Serveur)

Le serveur REST est le cœur du système et expose les fonctionnalités via des endpoints HTTP.
Il est responsable de :

- Fournir l’accès au catalogue officiel des cours et aux horaires (via Planifium)

- Agréger les résultats académiques à partir des fichiers CSV

- Gérer la persistance et la consultation des avis étudiants

- Valider les requêtes et paramètres reçus

- Appliquer la logique métier (comparaisons, agrégations, calculs)

- Gérer la création et l’analyse des ensembles de cours

- Exposer une interface stable et réutilisable par différents clients (CLI, bot, future interface graphique)

## Endpoints de l'API REST

Voici quelques endpoints principaux exposés par l'API :

### Cours

- `GET /courses` - Rechercher des cours (paramètres : `courses_sigle`, `sigle_prefix`, `name`, `description`)
- `GET /courses/{sigle}` - Détails d'un cours
- `GET /courses/offered?semester=A25` - Cours offerts pour un trimestre
- `GET /courses/{sigle}/eligibility?cycle=1&completed=IFT1015` - Vérifier l'éligibilité
- `GET /courses/{sigle}/results` - Résultats académiques d'un cours
- `GET /courses/compare-full?ids=IFT2255,IFT2125` - Comparer des cours

### Avis étudiants

- `GET /avis/{courseId}` - Lister les avis pour un cours
- `GET /avis/{courseId}/aggregate` - Agrégat des avis (moyennes)
- `POST /avis` - Ajouter un avis (body : `courseId`, `difficulty`, `workload`, `comment`)

### Ensembles de cours

- `POST /sets` - Créer un ensemble (body : `semester`, `courseIds`)
- `GET /sets/{id}` - Consulter un ensemble
- `GET /sets/{id}/schedule` - Horaire d'un ensemble
- `GET /sets/{id}/conflicts` - Détecter les conflits d'horaire

### Utilisateurs

- `GET /users` - Lister les utilisateurs
- `GET /users/{id}` - Détails d'un utilisateur

**Note:** Tous les endpoints retournent du JSON. L'API est documentée dans le code source (`Routes.java` et contrôleurs).

## Lancer les tests

Tous les tests unitaires sont situés dans \`src/test/java\`.

**Lancer tous les tests**

```bash
mvn test
```

**Lancer une classe de tests spécifique**

```bash
mvn -Dtest=CourseServiceTest test
```

## Bot Discord – Avis étudiants

Les avis étudiants sont collectés via un bot Discord minimal, conformément à l'énoncé.

### Fonctionnalités

- Collecte d'avis étudiants depuis un canal Discord dédié
- Transmission des avis à l'API REST via `POST /avis`

### Configuration et exécution du bot

**Prérequis**

- Python 3.8+
- Token Discord Bot (obtenu via Discord Developer Portal)

**Installation des dépendances**

```bash
cd discord-bot
python -m venv venv
source venv/bin/activate  # Sur Windows : venv\Scripts\activate
pip install -r requirements.txt
```

**Configuration**

Créer un fichier `.env` dans le dossier `discord-bot/` :

```bash
cp .env.example .env
```

Éditer `.env` et ajouter votre token Discord :

```
DISCORD_TOKEN=votre_token_ici
API_BASE_URL=http://localhost:7070
```

**Lancer le bot**

```bash
python bot.py
```

**Utilisation**

Dans le canal Discord `#avis-cours`, utiliser la commande :

```
!avis IFT2255 4 5 Excellent cours, très formateur!
```

### Serveur Discord de test

https://discord.gg/YRGSyEAh

### Canal utilisé

- `#avis-cours`

### Documentation complète

Pour plus de détails sur le bot Discord (commandes, architecture, exemples), consultez :

**`discord-bot/README.md`**
