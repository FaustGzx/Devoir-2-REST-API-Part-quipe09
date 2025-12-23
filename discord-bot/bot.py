#!/usr/bin/env python3
"""
Bot Discord minimal pour IFT2255 - Devoir 3
Collecte les avis étudiants et les envoie à l'API REST.

Format de commande:
    !avis IFT2255 4 3 Commentaire optionnel ici...
    
    - IFT2255 = code du cours
    - 4 = difficulté (1-5)
    - 3 = charge de travail (1-5)
    - Commentaire = optionnel

Lancement:
    python bot.py
"""

import os
import re
import requests
import discord
from datetime import datetime
from dotenv import load_dotenv

# Charger les variables d'environnement
load_dotenv()

DISCORD_TOKEN = os.getenv("DISCORD_TOKEN", "").strip()
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:7070").rstrip("/")
ALLOWED_CHANNEL = os.getenv("ALLOWED_CHANNEL", "avis-cours").strip()

if not DISCORD_TOKEN:
    raise RuntimeError("❌ DISCORD_TOKEN manquant. Mets-le dans ton fichier .env")

# Configuration Discord
intents = discord.Intents.default()
intents.message_content = True  # IMPORTANT: nécessaire pour lire le contenu des messages

client = discord.Client(intents=intents)

# Pattern pour parser les avis
# Format: !avis IFT2255 4 3 commentaire optionnel
AVIS_PATTERN = re.compile(
    r"^!avis\s+([A-Za-z]{2,4}\d{4}[A-Za-z]?)\s+([1-5])\s+([1-5])(?:\s+(.*))?$",
    re.IGNORECASE
)


def post_review_to_api(payload: dict) -> requests.Response:
    """Envoie un avis à l'API REST."""
    url = f"{API_BASE_URL}/avis"
    return requests.post(url, json=payload, timeout=10)


@client.event
async def on_ready():
    """Appelé quand le bot est connecté et prêt."""
    print("=" * 50)
    print(f"✅ Bot connecté en tant que: {client.user}")
    print(f"📡 API_BASE_URL: {API_BASE_URL}")
    print(f"📢 Canal autorisé: #{ALLOWED_CHANNEL}")
    print("=" * 50)
    print("\n📝 Format des avis:")
    print("   !avis IFT2255 4 3 Commentaire optionnel")
    print("   - difficulté: 1-5")
    print("   - charge: 1-5")
    print("\n⏳ En attente de messages...")


@client.event
async def on_message(message: discord.Message):
    """Traite chaque message reçu."""
    
    # Ignorer les messages du bot lui-même
    if message.author == client.user:
        return

    # Limiter au canal autorisé (si configuré)
    if ALLOWED_CHANNEL and message.channel.name != ALLOWED_CHANNEL:
        return

    content = message.content.strip()

    # Commande d'aide
    if content.lower() in ("!avis-help", "!avis help", "!help-avis"):
        help_text = (
            "**📚 Bot Avis IFT2255**\n\n"
            "**Format:** `!avis CODE DIFF CHARGE [commentaire]`\n\n"
            "**Exemple:**\n"
            "```\n!avis IFT2255 4 3 Bon cours mais demandant.\n```\n\n"
            "**Paramètres:**\n"
            "• `CODE` — Sigle du cours (ex: IFT2255)\n"
            "• `DIFF` — Difficulté perçue (1 = facile, 5 = difficile)\n"
            "• `CHARGE` — Charge de travail (1 = légère, 5 = lourde)\n"
            "• `commentaire` — Optionnel, votre avis textuel\n\n"
            "**Exemples valides:**\n"
            "• `!avis IFT1015 2 2`\n"
            "• `!avis MAT1400 5 5 Très difficile!`\n"
            "• `!avis IFT2255 3 4 Projet intéressant mais long`"
        )
        await message.channel.send(help_text)
        return

    # Vérifier si c'est une commande !avis
    match = AVIS_PATTERN.match(content)
    if not match:
        # Si le message commence par !avis mais ne correspond pas au format
        if content.lower().startswith("!avis"):
            await message.channel.send(
                "❌ Format invalide. Utilise: `!avis IFT2255 4 3 commentaire optionnel`\n"
                "Tape `!avis-help` pour plus d'infos."
            )
        return

    # Extraire les données
    course_code = match.group(1).upper()
    difficulty = int(match.group(2))
    workload = int(match.group(3))
    comment = (match.group(4) or "").strip()

    # Construire le payload pour l'API
    # Compatible avec le modèle Review.java existant
    payload = {
        "courseId": course_code,
        "difficulty": difficulty,
        "workload": workload,
        "comment": comment,
        "author": message.author.display_name,  # Nom Discord
        "timestamp": int(datetime.now().timestamp() * 1000)  # Epoch ms
    }

    # Log côté serveur
    print(f"\n📨 Nouvel avis de {message.author.display_name}:")
    print(f"   Cours: {course_code}")
    print(f"   Difficulté: {difficulty}/5")
    print(f"   Charge: {workload}/5")
    print(f"   Commentaire: {comment or '(aucun)'}")

    # Envoyer à l'API
    try:
        response = post_review_to_api(payload)
        
        if 200 <= response.status_code < 300:
            await message.add_reaction("✅")
            print(f"   ✅ Envoyé avec succès!")
            
            # Message de confirmation
            confirm_msg = (
                f"✅ **Avis enregistré!**\n"
                f"• Cours: `{course_code}`\n"
                f"• Difficulté: {'⭐' * difficulty}\n"
                f"• Charge: {'📚' * workload}"
            )
            await message.channel.send(confirm_msg)
        else:
            await message.add_reaction("❌")
            error_detail = response.text[:200] if response.text else "Pas de détails"
            print(f"   ❌ Erreur API: {response.status_code}")
            await message.channel.send(
                f"❌ Erreur API ({response.status_code}): {error_detail}"
            )
            
    except requests.exceptions.ConnectionError:
        await message.add_reaction("⚠️")
        print(f"   ⚠️ API non accessible!")
        await message.channel.send(
            "⚠️ Impossible de joindre l'API. Vérifiez que le backend est démarré."
        )
    except requests.exceptions.Timeout:
        await message.add_reaction("⏱️")
        print(f"   ⏱️ Timeout API!")
        await message.channel.send("⏱️ L'API met trop de temps à répondre.")
    except Exception as e:
        await message.add_reaction("💥")
        print(f"   💥 Erreur inattendue: {e}")
        await message.channel.send(f"💥 Erreur inattendue: {e}")


if __name__ == "__main__":
    print("\n🚀 Démarrage du bot Discord IFT2255...")
    client.run(DISCORD_TOKEN)
