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
│   │   │   │   ├── CourseController.java # Contrôleur pour les endpoints de cours
│   │   │   │   └── UserController.java   # Contrôleur pour les endpoints utilisateurs
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Course.java           # Modèle représentant un cours
│   │   │   │   └── User.java             # Modèle représentant un utilisateur
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── CourseService.java    # Logique métier liée aux cours
│   │   │   │   └── UserService.java      # Logique métier liée aux utilisateurs
│   │   │   │
│   │   │   ├── util/
│   │   │   │   ├── HttpClientAPI.java    # Client HTTP pour appels externes
│   │   │   │   ├── HttpResponse.java     # Représentation d'une réponse HTTP
│   │   │   │   ├── HttpStatus.java       # Codes de statut HTTP
│   │   │   │   ├── ResponseUtil.java     # Outils pour formater les réponses
│   │   │   │   └── ValidationUtil.java   # Méthodes utilitaires de validation
│   │   │   │
│   │   │   └── Main.java                 # Point d’entrée du serveur Javalin
│   │   │
│   │   └── resources/                    # Ressources utilisées dans le code
│   │
│   └── test/                             # Tests unitaires (JUnit)
│
└── pom.xml
```

## Architecture

Ce template suit principalement le modèle MVC :

- **Model (`model/`)** : Représentation des entités du domaine (ex. User, Course)
- **Controller (`controller/`)** : Gestion des requêtes HTTP et appels au service
- **Service (`service/`)** : Logique métier centrale
- **Util (`util/`)** : Fonctions utilitaires réutilisables (validation, réponses, etc.)
- **Config (`config/`)** : Configuration du serveur et définition des routes
- **`Main.java`** : Point d’entrée du serveur (initialise Javalin et enregistre les routes)

## Bonnes pratiques

- Respectez la séparation des responsabilités entre **Controller**, **Service** et **Model**.
- Utilisez les classes du dossier `util/` pour les validations et la gestion des réponses HTTP.
- Centralisez les routes dans `config/Routes.java` pour simplifier l’ajout de nouveaux endpoints.
- Ajoutez des **tests unitaires** pour chaque méthode de service.
- Conservez un style de code uniforme (respect du standard Java).


## Prérequis

- **Java 17**
- **Maven 3.x**

## Vérifier les installations :

java -version
mvn -v

## Installation:

**Cloner le dépôt puis se placer dans le module** :

git clone <https://github.com/FaustGzx/ift2255-devoir2.git>
cd ift2255-devoir2/rest-api

**Télécharger les dépendances** :

mvn clean compile
## Exécution de l’application :

1. **Lancer le serveur REST (Javalin)**

cd ift2255-devoir2/rest-api
mvn exec:java -Dexec.mainClass="com.diro.ift2255.Main"
Serveur disponible à : http://localhost:7070

2. **Lancer la CLI**

cd ift2255-devoir2/rest-api
mvn exec:java -Dexec.mainClass="com.diro.ift2255.cli.CliApp"

La CLI permet de :

Rechercher des cours

Consulter les détails

Vérifier l’éligibilité

Comparer des cours et estimer la charge totale

## Lancer les tests
Tous les tests JUnit se trouvent dans src/test/java.

**Lancer tous les tests** :

mvn test
**Lancer uniquement une classe de tests** :

mvn -Dtest=CourseServiceTest test
