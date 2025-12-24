package com.diro.ift2255.cli;

import java.util.Scanner;

/**
 * Gestion des menus et de la navigation dans le CLI.
 * Affiche les options et récupère les choix utilisateur.
 */
public class CliMenu {

    private final Scanner scanner;
    /**
    * Construit le menu CLI avec un scanner pour lire les entrées utilisateur.
    *
    * @param scanner scanner utilisé pour lire l’entrée standard
    */
    public CliMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    // ========================================================================
    // Menu principal
    // ========================================================================

    public void printMainMenu() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║           MENU PRINCIPAL                         ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  1. Rechercher des cours                         ║");
        System.out.println("║  2. Détails d'un cours                           ║");
        System.out.println("║  3. Cours offerts (par trimestre)                ║");
        System.out.println("║  4. Horaire d'un cours                           ║");
        System.out.println("║  5. Vérifier éligibilité à un cours              ║");
        System.out.println("║  6. Résultats académiques d'un cours             ║");
        System.out.println("║  7. Avis étudiants                               ║");
        System.out.println("║  8. Comparer des cours                           ║");
        System.out.println("║  9. Ensembles de cours                           ║");
        System.out.println("║  0. Quitter                                      ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.print("Votre choix : ");
    }
    /**
    * Demande à l’utilisateur de choisir une option du menu principal.
    *
    * @return choix saisi par l’utilisateur
    */
    public String getMainMenuChoice() {
        return scanner.nextLine().trim();
    }
    

    // ========================================================================
    // Sous-menu Recherche
    // ========================================================================

    public void printSearchMenu() {
        System.out.println();
        System.out.println("── Mode de recherche ──");
        System.out.println("  1. Par sigle (ex: IFT2255)");
        System.out.println("  2. Par préfixe de sigle (ex: IFT)");
        System.out.println("  3. Par nom du cours");
        System.out.println("  4. Par mots-clés (description)");
        System.out.println("  0. Retour au menu principal");
        System.out.print("Choix : ");
    }

    // ========================================================================
    // Sous-menu Avis
    // ========================================================================

    public void printReviewMenu() {
        System.out.println();
        System.out.println("── Avis étudiants ──");
        System.out.println("  1. Voir les avis d'un cours");
        System.out.println("  2. Voir l'agrégat (moyennes)");
        System.out.println("  3. Ajouter un avis");
        System.out.println("  0. Retour au menu principal");
        System.out.print("Choix : ");
    }

    // ========================================================================
    // Sous-menu Ensembles
    // ========================================================================

    public void printSetMenu() {
        System.out.println();
        System.out.println("── Ensembles de cours ──");
        System.out.println("  1. Créer un nouvel ensemble");
        System.out.println("  2. Voir un ensemble existant");
        System.out.println("  3. Voir l'horaire d'un ensemble");
        System.out.println("  4. Détecter les conflits (BONUS)");
        System.out.println("  0. Retour au menu principal");
        System.out.print("Choix : ");
    }

    // ========================================================================
    // Entrées utilisateur avec validation
    // ========================================================================
    /**
    * Demande une entrée texte à l’utilisateur.
    *
    * @param prompt message affiché à l’utilisateur
    * @return texte saisi par l’utilisateur
    */
    public String askForInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String askForSigle(String prompt) {
        System.out.print(prompt != null ? prompt : "Code du cours (ex: IFT2255) : ");
        String input = scanner.nextLine().trim().toUpperCase();
        
        // Validation basique du format
        if (input.isEmpty()) {
            return null;
        }
        
        return input;
    }
    /**
    * Demande à l’utilisateur de saisir un trimestre.
    *
    * @param prompt message affiché à l’utilisateur
    * @return code du trimestre (ex : H25, A24, E24)
    */
    public String askForSemester(String prompt) {
        System.out.print(prompt != null ? prompt : "Trimestre (ex: H25, A25, E25) : ");
        String input = scanner.nextLine().trim().toUpperCase();
        
        if (input.isEmpty()) {
            return null;
        }
        
        // Validation du format
        if (!input.matches("^[HAE]\\d{2}$")) {
            CliPrinter.printError("Format invalide. Utilisez H25, A25 ou E25.");
            return null;
        }
        
        return input;
    }
    /**
    * Demande une note numérique à l’utilisateur dans un intervalle donné.
    *
    * @param prompt message affiché à l’utilisateur
    * @param min valeur minimale acceptée
    * @param max valeur maximale acceptée
    * @return valeur saisie par l’utilisateur
    */
    public int askForRating(String prompt, int min, int max) {
        System.out.print(prompt + " (" + min + "-" + max + ") : ");
        try {
            int value = Integer.parseInt(scanner.nextLine().trim());
            if (value < min || value > max) {
                CliPrinter.printError("La valeur doit être entre " + min + " et " + max + ".");
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            CliPrinter.printError("Veuillez entrer un nombre.");
            return -1;
        }
    }

    public String askForCycle() {
        System.out.println("Cycle d'études :");
        System.out.println("  1. Premier cycle (Baccalauréat)");
        System.out.println("  2. Cycles supérieurs (Maîtrise/Doctorat)");
        System.out.print("Choix : ");
        
        String choice = scanner.nextLine().trim();
        return switch (choice) {
            case "1" -> "1";
            case "2" -> "2";
            default -> null;
        };
    }

    public String[] askForCourseList(String prompt) {
        System.out.println(prompt);
        System.out.println("(Séparez les cours par des virgules, ex: IFT1015, IFT1025, MAT1400)");
        System.out.print("> ");
        
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return new String[0];
        }
        
        return input.split("\\s*,\\s*");
    }

    // ========================================================================
    // Messages de navigation
    // ========================================================================

    public void printWelcome(String baseUrl) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║     OUTIL DE CHOIX DE COURS - IFT2255            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println("  API : " + baseUrl);
        System.out.println();
    }

    public void printGoodbye() {
        System.out.println();
        System.out.println("Au revoir!");
    }

    public void printInvalidOption() {
        CliPrinter.printError("Option invalide. Veuillez réessayer.");
    }

    public void pressEnterToContinue() {
        System.out.println();
        System.out.print("Appuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }
}
