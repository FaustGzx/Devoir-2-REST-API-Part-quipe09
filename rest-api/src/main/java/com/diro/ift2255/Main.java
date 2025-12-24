package com.diro.ift2255;

import io.javalin.Javalin;

import com.diro.ift2255.config.Routes;
/**
 * Point d’entrée principal du serveur REST de l’application.
 *
 * <p>Cette classe initialise le serveur Javalin,
 * configure les routes de l’API et démarre le serveur HTTP.</p>
 */

public class Main {
     /**
     * Méthode principale de démarrage de l’application serveur.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Crée une instance de Javalin avec une configuration personnalisée
        // Ici, on définit le type de contenu par défaut des réponses HTTP en JSON
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        });

        // Enregistre toutes les routes de l'application
        Routes.register(app);

        // Démarre le serveur sur le port 7070 (choix arbitraire)
        app.start(7070);
        System.out.println("Serveur démarré sur http://localhost:7070");
    }
}