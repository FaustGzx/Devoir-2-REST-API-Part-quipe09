package com.diro.ift2255.cli;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.EligibilityResult;
import com.diro.ift2255.service.CourseService;
import com.diro.ift2255.util.HttpClientApi;

import java.util.*;

/**
 * Petite interface en ligne de commande pour jouer avec l'API Planifium
 * via notre CourseService.
 *
 * Pour lancer :
 *   mvn exec:java -Dexec.mainClass="com.diro.ift2255.cli.CliApp"
 */
public class CliApp {

    private final Scanner scanner;
    private final CourseService courseService;

    public CliApp() {
        this.scanner = new Scanner(System.in);
        this.courseService = new CourseService(new HttpClientApi());
    }

    public static void main(String[] args) {
        CliApp app = new CliApp();
        app.run();
    }

    /**
     * Boucle principale : affiche le menu et redirige vers les actions.
     */
    public void run() {
        boolean running = true;

        System.out.println("====================================");
        System.out.println("  Outil de choix de cours (CLI)");
        System.out.println("====================================");

        while (running) {
            System.out.println();
            System.out.println("=== Menu principal ===");
            System.out.println("1. Rechercher des cours");
            System.out.println("2. Voir les détails d'un cours");
            System.out.println("3. Vérifier mon éligibilité à un cours");
            System.out.println("4. Comparer plusieurs cours");
            System.out.println("0. Quitter");
            System.out.print("Choix : ");

            String choix = scanner.nextLine().trim();

            switch (choix) {
                case "1" -> handleSearchCourses();
                case "2" -> handleShowCourseDetails();
                case "3" -> handleCheckEligibility();
                case "4" -> handleCompareCourses();
                case "0" -> {
                    running = false;
                    System.out.println("Au revoir!");
                }
                default -> System.out.println("Option invalide, essaie encore.");
            }
        }

        scanner.close();
    }

    // =========================================================
    // 1) Recherche de cours (CU Recherche)
    // =========================================================
    private void handleSearchCourses() {
    System.out.println("\n=== Recherche de cours ===");
    System.out.println("Tu peux chercher par : ");
    System.out.println("  1. Code (sigle, ex: IFT2255)");
    System.out.println("  2. Nom du cours");
    System.out.println("  3. Mots-clés dans la description");
    System.out.print("Choix (ou tape directement un code ex: IFT2015) : ");

    String firstInput = scanner.nextLine().trim();

    String mode;
    String term;

    // Si l'utilisateur tape directement un truc du style IFTxxxx,
    // on le prend comme recherche par code (mode 1).
    if (!firstInput.equals("1") && !firstInput.equals("2") && !firstInput.equals("3")) {
        mode = "1";         // recherche par sigle
        term = firstInput;  // ce qu'il a tapé (ex: IFT2015)
    } else {
        mode = firstInput;
        System.out.print("Terme de recherche : ");
        term = scanner.nextLine().trim();
    }

    if (term.isBlank()) {
        System.out.println("Terme vide, retour au menu.");
        return;
    }

    Map<String, String> params = new HashMap<>();

    switch (mode) {
        case "1" -> params.put("sigle", term);        // recherche par code
        case "2" -> params.put("name", term);         // recherche par nom
        case "3" -> params.put("description", term);  // recherche par desc
        default -> {
            System.out.println("Mode de recherche invalide, retour au menu.");
            return;
        }
    }

    List<Course> courses = courseService.getAllCourses(params);

    if (courses.isEmpty()) {
        System.out.println("Aucun cours trouvé pour la recherche : " + term);
        return;
    }

    System.out.println("\nRésultats : ");
    for (Course c : courses) {
        System.out.println("-------------------------------------");
        System.out.println(c.getId() + " - " + c.getName());
        if (c.getDescription() != null) {
            String desc = c.getDescription();
            if (desc.length() > 150) {
                desc = desc.substring(0, 150) + "...";
            }
            System.out.println(desc);
        }
        if (c.getCredits() != null) {
            System.out.println("Crédits : " + c.getCredits());
        }
    }
}


    // =========================================================
    // 2) Détails d'un cours (CU Voir les détails)
    // =========================================================
    private void handleShowCourseDetails() {
        System.out.println();
        System.out.println("=== Détails d'un cours ===");
        System.out.print("Code du cours (ex: IFT2255) : ");
        String code = scanner.nextLine().trim();

        if (code.isEmpty()) {
            System.out.println("Code vide, retour au menu.");
            return;
        }

        // On propose une session, par défaut A25 si vide
        System.out.print("Session pour l'horaire (ex: A25, laisse vide pour A25) : ");
        String semester = scanner.nextLine().trim();
        if (semester.isEmpty()) {
            semester = "A25";
        }

        Map<String, String> params = new HashMap<>();
        params.put("include_schedule", "true");
        params.put("schedule_semester", semester);

        try {
            Optional<Course> opt = courseService.getCourseById(code, params);
            if (opt.isEmpty()) {
                System.out.println("Aucun cours trouvé pour " + code);
                return;
            }

            Course c = opt.get();
            System.out.println();
            System.out.println(c.getId() + " - " + c.getName());
            System.out.println("-------------------------------------");
            System.out.println(c.getDescription());
            System.out.println();
            System.out.println("Crédits : " + (c.getCredits() != null ? c.getCredits() : "?"));

            // Pré-requis
            System.out.println("Pré-requis : " + buildPrereqDisplay(c));


            // Sessions offertes
            if (c.getAvailableTerms() != null && !c.getAvailableTerms().isEmpty()) {
                System.out.println("Sessions offertes : " + formatTerms(c.getAvailableTerms()));
            }

            // Périodes
            if (c.getAvailablePeriods() != null && !c.getAvailablePeriods().isEmpty()) {
                System.out.println("Périodes : " + formatPeriods(c.getAvailablePeriods()));
            }

            // Mini aperçu de l'horaire si on a des schedules
            if (c.getSchedules() != null && !c.getSchedules().isEmpty()) {
                System.out.println();
                System.out.println("Sections disponibles (" + semester + ") :");
                c.getSchedules().forEach(schedule -> {
                    if (schedule.getSections() == null) return;
                    schedule.getSections().forEach(section -> {
                        System.out.print("  - Section " + section.getName());
                        if (section.getTeachers() != null && !section.getTeachers().isEmpty()) {
                            System.out.print(" | Prof(s) : " + String.join(", ", section.getTeachers()));
                        }
                        if (section.getCapacity() != null) {
                            System.out.print(" | Capacité : " + section.getCapacity());
                        }
                        System.out.println();
                    });
                });
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération du cours : " + e.getMessage());
        }
    }

    // =========================================================
    // 3) Éligibilité (CU Recherche de cours – voir si éligible)
    // =========================================================
    private void handleCheckEligibility() {
        System.out.println();
        System.out.println("=== Vérifier mon éligibilité à un cours ===");
        System.out.print("Code du cours (ex: IFT2255) : ");
        String code = scanner.nextLine().trim();

        if (code.isEmpty()) {
            System.out.println("Code vide, retour au menu.");
            return;
        }

        System.out.println("Entre les cours que tu as déjà réussis, séparés par des virgules.");
        System.out.println("Exemple : IFT1015, IFT1025");
        System.out.print("> ");

        String completedRaw = scanner.nextLine();
        List<String> completed = parseCodeList(completedRaw);

        try {
            EligibilityResult result = courseService.checkEligibility(code, completed);

            System.out.println();
            System.out.println("Résultat pour " + code + " :");
            System.out.println("- Éligible : " + (result.isEligible() ? "OUI" : "NON"));

            if (!result.getMissingPrerequisites().isEmpty()) {
                System.out.println("- Pré-requis manquants : "
                        + String.join(", ", result.getMissingPrerequisites()));
            } else {
                System.out.println("- Pré-requis manquants : (aucun)");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du calcul d'éligibilité : " + e.getMessage());
        }
    }

    // =========================================================
    // 4) Comparaison de cours + charge totale (CU Comparer)
    // =========================================================
    private void handleCompareCourses() {
        System.out.println();
        System.out.println("=== Comparer plusieurs cours ===");
        System.out.println("Entre une liste de codes séparés par des virgules.");
        System.out.println("Exemple : IFT3700, IFT2035");
        System.out.print("> ");

        String raw = scanner.nextLine();
        List<String> codes = parseCodeList(raw);

        if (codes.isEmpty()) {
            System.out.println("Aucun code valide, retour au menu.");
            return;
        }

        // On force l'inclusion des horaires pour une session (au besoin plus tard)
        String semester = "A25"; // valeur par défaut pour la comparaison
        Map<String, String> params = new HashMap<>();
        params.put("include_schedule", "true");
        params.put("schedule_semester", semester);

        try {
            List<Course> courses = courseService.compareCourses(codes, params);
            if (courses.isEmpty()) {
                System.out.println("Aucun cours trouvé pour ces codes.");
                return;
            }

            System.out.println();
            System.out.println("=== Résumé des cours ===");
            for (Course c : courses) {
                System.out.println("-------------------------------------");
                System.out.println(c.getId() + " - " + c.getName());
                System.out.println("Crédits : " + (c.getCredits() != null ? c.getCredits() : "?"));
                
                // Pré-requis
                System.out.println("Pré-requis : " + buildPrereqDisplay(c));


                if (c.getAvailableTerms() != null && !c.getAvailableTerms().isEmpty()) {
                    System.out.println("Sessions : " + formatTerms(c.getAvailableTerms()));
                }
                if (c.getAvailablePeriods() != null && !c.getAvailablePeriods().isEmpty()) {
                    System.out.println("Périodes : " + formatPeriods(c.getAvailablePeriods()));
                }
            }

            // Calcul de la charge totale (somme des crédits)
            double totalCredits = courses.stream()
                    .map(Course::getCredits)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            System.out.println();
            System.out.println("Charge totale estimée pour cette combinaison : "
                    + totalCredits + " crédits");

            if (totalCredits > 12) {
                System.out.println("⚠ Attention : plus de 12 crédits, ça commence à être chargé.");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la comparaison : " + e.getMessage());
        }
    }

    // =========================================================
    // Petites méthodes utilitaires
    // =========================================================

    /**
     * Parse une chaîne du type "IFT2255, IFT2015" en liste de codes propres.
     */
    private List<String> parseCodeList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String[] parts = raw.split(",");
        List<String> result = new ArrayList<>();

        for (String p : parts) {
            if (p != null) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    private String formatTerms(Map<String, Boolean> terms) {
        List<String> active = new ArrayList<>();
        if (Boolean.TRUE.equals(terms.get("autumn"))) active.add("automne");
        if (Boolean.TRUE.equals(terms.get("winter"))) active.add("hiver");
        if (Boolean.TRUE.equals(terms.get("summer"))) active.add("été");
        return active.isEmpty() ? "aucune info" : String.join(", ", active);
    }

    private String formatPeriods(Map<String, Boolean> periods) {
        List<String> active = new ArrayList<>();
        if (Boolean.TRUE.equals(periods.get("daytime"))) active.add("jour");
        if (Boolean.TRUE.equals(periods.get("evening"))) active.add("soir");
        return active.isEmpty() ? "aucune info" : String.join(", ", active);
    }

        /**
     * Retourne une version "propre" des prérequis à afficher.
     * - Si requirement_text est présent, on le nettoie (on enlève le préfixe moche).
     * - Sinon on utilise la liste prerequisite_courses.
     * - Sinon "Aucun".
     */
    private String buildPrereqDisplay(Course c) {
        String txt = c.getRequirementText();
        List<String> prereqs = c.getPrerequisiteCourses();

        // 1) On essaie d'utiliser requirement_text si dispo
        if (txt != null && !txt.isBlank()) {
            String cleaned = txt.trim();

            // Souvent ça ressemble à : "prerequisite_courses :  IFT2015 ET (MAT1978 OU ...)"
            String lower = cleaned.toLowerCase(Locale.ROOT);
            int idx = lower.indexOf("prerequisite_courses");
            if (idx != -1) {
                int colonIdx = cleaned.indexOf(':', idx);
                if (colonIdx != -1 && colonIdx + 1 < cleaned.length()) {
                    cleaned = cleaned.substring(colonIdx + 1).trim();
                }
            }

            // On retourne juste "IFT2015 ET (MAT1978 OU MAT1720 OU STT1700)"
            return cleaned;
        }

        // 2) Sinon on tombe back sur la liste brute
        if (prereqs != null && !prereqs.isEmpty()) {
            return String.join(", ", prereqs);
        }

        // 3) Sinon rien
        return "Aucun";
    }

}
