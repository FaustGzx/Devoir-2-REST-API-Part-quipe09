# Bot Discord - IFT2255 Avis Étudiants

Bot Discord minimal pour collecter les avis étudiants et les soumettre à l'API REST.

## Prérequis

- Python 3.10+
- Un bot Discord configuré sur [Discord Developer Portal](https://discord.com/developers/applications)
- Le backend REST API démarré (`http://localhost:7070`)

## Installation

```bash
cd discord-bot
python3 -m venv .venv
source .venv/bin/activate    # Linux/Mac
# ou: .venv\Scripts\activate  # Windows

pip install -r requirements.txt
```

## Configuration

1. Copiez le fichier `.env.example` vers `.env` :
   ```bash
   cp .env.example .env
   ```

2. Modifiez `.env` avec votre token Discord :
   ```env
   DISCORD_TOKEN=votre_token_ici
   API_BASE_URL=http://localhost:7070
   ALLOWED_CHANNEL=avis-cours
   ```

## Lancement

```bash
# Activer l'environnement virtuel
source .venv/bin/activate

# Lancer le bot
python bot.py
```

## Utilisation

Dans le canal `#avis-cours` de votre serveur Discord :

### Lien du serveur Discord de test
https://discord.gg/YRGSyEAh

### Format de commande
```
!avis CODE DIFFICULTÉ CHARGE [commentaire]
```

### Exemples
```
!avis IFT2255 4 3 Très bon cours mais demandant.
!avis IFT1015 2 2 Cours d'intro facile
!avis MAT1400 5 5 Difficile!
```

### Paramètres
| Paramètre | Description | Valeurs |
|-----------|-------------|---------|
| `CODE` | Sigle du cours | ex: IFT2255 |
| `DIFFICULTÉ` | Difficulté perçue | 1 (facile) à 5 (difficile) |
| `CHARGE` | Charge de travail | 1 (légère) à 5 (lourde) |
| `commentaire` | Avis textuel | Optionnel |

### Commandes
- `!avis ...` — Soumettre un avis
- `!avis-help` — Afficher l'aide

## Serveur Discord de Test

### Lien du serveur Discord de test
https://discord.gg/YRGSyEAh

Canal dédié : `#avis-cours`

## Données de test requises

Selon l'énoncé, il faut **minimum 3-5 avis** pour **au moins 5 cours** 
