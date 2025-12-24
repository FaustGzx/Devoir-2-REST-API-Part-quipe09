package com.diro.ift2255.cli;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * Application CLI pour interagir avec l'API REST de choix de cours.
 * 
 * Le CLI ne contient PAS de logique métier, il appelle l'API et affiche les résultats.
 * 
 * Lancement :
 *   mvn exec:java@cli
 *   
 * Avec URL personnalisée :
 *   mvn exec:java@cli -Dexec.args="--baseUrl=http://localhost:7070"
 */
public class CliApp {

    private static final String DEFAULT_BASE_URL = "http://localhost:7070";

    private final ApiClient apiClient;
    private final CliMenu menu;
    private final Scanner scanner;
    private boolean running;

    public CliApp(String baseUrl) {
        this.scanner = new Scanner(System.in);
        this.apiClient = new ApiClient(baseUrl);
        this.menu = new CliMenu(scanner);
        this.running = true;
    }

    public static void main(String[] args) {
        String baseUrl = DEFAULT_BASE_URL;

        // Parser les arguments
        for (String arg : args) {
            if (arg.startsWith("--baseUrl=")) {
                baseUrl = arg.substring("--baseUrl=".length());
            }
        }

        // Vérifier variable d'environnement
        String envUrl = System.getenv("API_BASE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            baseUrl = envUrl;
        }

        CliApp app = new CliApp(baseUrl);
        app.run();
    }

    /**
     * Boucle principale du CLI.
     */
    public void run() {
        menu.printWelcome(apiClient.getBaseUrl());

        // Sanity check : tester la connexion à l'API
        if (!testApiConnection()) {
            CliPrinter.printError("Impossible de se connecter à l'API sur " + apiClient.getBaseUrl());
            CliPrinter.printInfo("Assurez-vous que le serveur est démarré (mvn exec:java@server)");
            return;
        }

        CliPrinter.printSuccess("Connexion à l'API établie !");

        while (running) {
            menu.printMainMenu();
            String choice = menu.getMainMenuChoice();

            switch (choice) {
                case "1" -> handleSearchCourses();
                case "2" -> handleCourseDetails();
                case "3" -> handleCoursesOffered();
                case "4" -> handleCourseSchedule();
                case "5" -> handleEligibility();
                case "6" -> handleAcademicResults();
                case "7" -> handleReviewsMenu();
                case "8" -> handleCompareCourses();
                case "9" -> handleSetsMenu();
                case "0" -> {
                    running = false;
                    menu.printGoodbye();
                }
                default -> menu.printInvalidOption();
            }
        }

        scanner.close();
    }

    /**
     * Test de connexion à l'API (sanity check).
     */
    private boolean testApiConnection() {
        try {
            ApiClient.ApiResponse response = apiClient.get("/users");
            return response.getStatusCode() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    // 1. Rechercher des cours
    // ========================================================================

    private void handleSearchCourses() {
        menu.printSearchMenu();
        String choice = menu.getMainMenuChoice();

        Map<String, String> params = new HashMap<>();
        String searchTerm;

        switch (choice) {
            case "1" -> {
                searchTerm = menu.askForSigle("Code exact du cours : ");
                if (searchTerm == null) return;
                params.put("courses_sigle", searchTerm);
            }
            case "2" -> {
                searchTerm = menu.askForInput("Préfixe du sigle (ex: IFT) : ").toUpperCase();
                if (searchTerm.isEmpty()) return;
                params.put("sigle_prefix", searchTerm);
            }
            case "3" -> {
                searchTerm = menu.askForInput("Nom du cours (mots-clés) : ");
                if (searchTerm.isEmpty()) return;
                params.put("name", searchTerm);
            }
            case "4" -> {
                searchTerm = menu.askForInput("Mots-clés dans la description : ");
                if (searchTerm.isEmpty()) return;
                params.put("description", searchTerm);
            }
            case "0" -> { return; }
            default -> {
                menu.printInvalidOption();
                return;
            }
        }

        CliPrinter.printTitle("Résultats de recherche");

        ApiClient.ApiResponse response = apiClient.get("/courses", params);

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        JsonNode data = response.getData();
        if (data == null || !data.isArray() || data.isEmpty()) {
            CliPrinter.printInfo("Aucun cours trouvé.");
        } else {
            CliPrinter.printInfo(data.size() + " cours trouvé(s) :");
            System.out.println();
            for (JsonNode course : data) {
                CliPrinter.printCourseShort(course);
            }
        }

        // Afficher message si présent
        if (response.getMessage() != null) {
            System.out.println();
            CliPrinter.printInfo(response.getMessage());
        }

        menu.pressEnterToContinue();
    }

    // ========================================================================
    // 2. Détails d'un cours
    // ========================================================================

    private void handleCourseDetails() {
        CliPrinter.printTitle("Détails d'un cours");

        String sigle = menu.askForSigle(null);
        if (sigle == null || sigle.isEmpty()) {
            CliPrinter.printError("Sigle vide, retour au menu.");
            return;
        }

        ApiClient.ApiResponse response = apiClient.get("/courses/" + sigle);

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        CliPrinter.printCourseDetailed(response.getData());
        menu.pressEnterToContinue();
    }

    // ========================================================================
    // 3. Cours offerts par trimestre
    // ========================================================================

    private void handleCoursesOffered() {
        CliPrinter.printTitle("Cours offerts par trimestre");

        String semester = menu.askForSemester(null);
        if (semester == null) return;

        Map<String, String> params = new HashMap<>();
        params.put("semester", semester);

        CliPrinter.printInfo("Recherche des cours offerts en " + semester + "...");

        ApiClient.ApiResponse response = apiClient.get("/courses/offered", params);

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        JsonNode data = response.getData();
        if (data == null || !data.isArray() || data.isEmpty()) {
            CliPrinter.printInfo("Aucun cours offert pour " + semester);
        } else {
            CliPrinter.printInfo(data.size() + " cours offert(s) en " + semester + " :");
            System.out.println();
            for (JsonNode course : data) {
                CliPrinter.printCourseShort(course);
            }
        }

        menu.pressEnterToContinue();
    }

    // ========================================================================
    // 4. Horaire d'un cours
    // ========================================================================

    private void handleCourseSchedule() {
        CliPrinter.printTitle("Horaire d'un cours");

        String sigle = menu.askForSigle(null);
        if (sigle == null || sigle.isEmpty()) return;

        String semester = menu.askForSemester(null);
        if (semester == null) return;

        Map<String, String> params = new HashMap<>();
        params.put("include_schedule", "true");
        params.put("schedule_semester", semester.toLowerCase());

        ApiClient.ApiResponse response = apiClient.get("/courses/" + sigle, params);

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        JsonNode course = response.getData();
        if (course == null) {
            CliPrinter.printError("Cours introuvable.");
            menu.pressEnterToContinue();
            return;
        }

        // Afficher infos de base
        String name = course.has("name") ? course.get("name").asText() : "";
        System.out.println();
        System.out.println("  📚 " + sigle + " - " + name);

        // Afficher horaire
        if (course.has("schedules")) {
            CliPrinter.printSchedule(course.get("schedules"), semester);
        } else {
            CliPrinter.printInfo("Aucun horaire disponible pour " + semester);
        }

        menu.pressEnterToContinue();
    }

    // ========================================================================
    // 5. Vérifier éligibilité
    // ========================================================================

    private void handleEligibility() {
        CliPrinter.printTitle("Vérifier éligibilité à un cours");

        String sigle = menu.askForSigle("Code du cours visé : ");
        if (sigle == null || sigle.isEmpty()) return;

        String cycle = menu.askForCycle();
        if (cycle == null) {
            CliPrinter.printError("Cycle invalide.");
            return;
        }

        String[] completed = menu.askForCourseList("Cours déjà réussis :");

        // Construire les paramètres
        Map<String, String> params = new HashMap<>();
        params.put("cycle", cycle);
        if (completed.length > 0) {
            params.put("completed", String.join(",", completed));
        }

        ApiClient.ApiResponse response = apiClient.get("/courses/" + sigle + "/eligibility", params);

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        CliPrinter.printEligibility(response.getData());
        menu.pressEnterToContinue();
    }

    // ========================================================================
    // 6. Résultats académiques
    // ========================================================================

    private void handleAcademicResults() {
        CliPrinter.printTitle("Résultats académiques d'un cours");

        String sigle = menu.askForSigle(null);
        if (sigle == null || sigle.isEmpty()) return;

        ApiClient.ApiResponse response = apiClient.get("/courses/" + sigle + "/results");

        if (!response.isSuccess()) {
            if (response.getStatusCode() == 404) {
                CliPrinter.printInfo("Aucun résultat académique disponible pour " + sigle);
                CliPrinter.printInfo("Les données ne sont pas toujours disponibles pour tous les cours.");
            } else {
                CliPrinter.printApiError(response);
            }
            menu.pressEnterToContinue();
            return;
        }

        CliPrinter.printAcademicResults(response.getData());
        menu.pressEnterToContinue();
    }

    // ========================================================================
    // 7. Avis étudiants (sous-menu)
    // ========================================================================

    private void handleReviewsMenu() {
        menu.printReviewMenu();
        String choice = menu.getMainMenuChoice();

        switch (choice) {
            case "1" -> handleViewReviews();
            case "2" -> handleViewReviewAggregate();
            case "3" -> handleAddReview();
            case "0" -> { }
            default -> menu.printInvalidOption();
        }
    }

    private void handleViewReviews() {
        CliPrinter.printTitle("Avis pour un cours");

        String sigle = menu.askForSigle(null);
        if (sigle == null || sigle.isEmpty()) return;

        ApiClient.ApiResponse response = apiClient.get("/avis/" + sigle);

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        CliPrinter.printReviews(response.getData());
        menu.pressEnterToContinue();
    }

    private void handleViewReviewAggregate() {
        CliPrinter.printTitle("Agrégat des avis");

        String sigle = menu.askForSigle(null);
        if (sigle == null || sigle.isEmpty()) return;

        ApiClient.ApiResponse response = apiClient.get("/avis/" + sigle + "/aggregate");

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        CliPrinter.printReviewAggregate(response.getData());
        menu.pressEnterToContinue();
    }

    private void handleAddReview() {
        CliPrinter.printTitle("Ajouter un avis");

        String sigle = menu.askForSigle(null);
        if (sigle == null || sigle.isEmpty()) return;

        int difficulty = menu.askForRating("Difficulté", 1, 5);
        if (difficulty == -1) return;

        int workload = menu.askForRating("Charge de travail", 1, 5);
        if (workload == -1) return;

        String comment = menu.askForInput("Commentaire (optionnel) : ");

        // Construire le body JSON
        Map<String, Object> body = new HashMap<>();
        body.put("courseId", sigle);
        body.put("difficulty", difficulty);
        body.put("workload", workload);
        if (!comment.isEmpty()) {
            body.put("comment", comment);
        }

        ApiClient.ApiResponse response = apiClient.post("/avis", body);

        if (response.isSuccess()) {
            CliPrinter.printSuccess("Avis ajouté avec succès !");
        } else {
            CliPrinter.printApiError(response);
        }

        menu.pressEnterToContinue();
    }

    // ========================================================================
    // 8. Comparer des cours
    // ========================================================================

    private void handleCompareCourses() {
        CliPrinter.printTitle("Comparer des cours");

        String[] courses = menu.askForCourseList("Cours à comparer :");
        if (courses.length < 2) {
            CliPrinter.printError("Veuillez entrer au moins 2 cours à comparer.");
            menu.pressEnterToContinue();
            return;
        }

        // Construire les paramètres
        Map<String, String> params = new HashMap<>();
        params.put("ids", String.join(",", courses));

        ApiClient.ApiResponse response = apiClient.get("/courses/compare-full", params);

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        JsonNode data = response.getData();
        
        // Afficher les cours comparés
        if (data != null && data.isArray()) {
            CliPrinter.printSubtitle("Cours comparés");
            double totalCredits = 0;
            
            for (JsonNode course : data) {
                CliPrinter.printCourseShort(course);
                if (course.has("credits") && !course.get("credits").isNull()) {
                    totalCredits += course.get("credits").asDouble();
                }
            }

            System.out.println();
            CliPrinter.printField("Total crédits", totalCredits);
            
            if (totalCredits > 15) {
                CliPrinter.printWarning("Attention : plus de 15 crédits, charge très élevée !");
            } else if (totalCredits > 12) {
                CliPrinter.printWarning("Attention : plus de 12 crédits, charge élevée.");
            }
        }

        menu.pressEnterToContinue();
    }

    // ========================================================================
    // 9. Ensembles de cours (sous-menu)
    // ========================================================================

    private void handleSetsMenu() {
        menu.printSetMenu();
        String choice = menu.getMainMenuChoice();

        switch (choice) {
            case "1" -> handleCreateSet();
            case "2" -> handleViewSet();
            case "3" -> handleSetSchedule();
            case "4" -> handleSetConflicts();
            case "0" -> { }
            default -> menu.printInvalidOption();
        }
    }

    private void handleCreateSet() {
        CliPrinter.printTitle("Créer un ensemble de cours");

        String semester = menu.askForSemester("Trimestre de l'ensemble : ");
        if (semester == null) return;

        String[] courses = menu.askForCourseList("Cours de l'ensemble (max 6) :");
        if (courses.length == 0) {
            CliPrinter.printError("Veuillez entrer au moins un cours.");
            return;
        }
        if (courses.length > 6) {
            CliPrinter.printError("Maximum 6 cours par ensemble.");
            return;
        }

        // Construire le body JSON
        Map<String, Object> body = new HashMap<>();
        body.put("semester", semester);
        body.put("courseIds", Arrays.asList(courses));

        ApiClient.ApiResponse response = apiClient.post("/sets", body);

        if (response.isSuccess()) {
            JsonNode data = response.getData();
            String setId = data.has("id") ? data.get("id").asText() : "?";
            CliPrinter.printSuccess("Ensemble créé avec succès !");
            CliPrinter.printField("ID de l'ensemble", setId);
            CliPrinter.printInfo("Conservez cet ID pour consulter l'ensemble plus tard.");
        } else {
            CliPrinter.printApiError(response);
        }

        menu.pressEnterToContinue();
    }

    private void handleViewSet() {
        CliPrinter.printTitle("Voir un ensemble de cours");

        String setId = menu.askForInput("ID de l'ensemble : ");
        if (setId.isEmpty()) return;

        ApiClient.ApiResponse response = apiClient.get("/sets/" + setId);

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        JsonNode data = response.getData();
        if (data != null) {
            CliPrinter.printField("ID", data.has("id") ? data.get("id").asText() : "?");
            CliPrinter.printField("Trimestre", data.has("semester") ? data.get("semester").asText() : "?");
            
            if (data.has("courseIds") && data.get("courseIds").isArray()) {
                System.out.println();
                CliPrinter.printSubtitle("Cours de l'ensemble");
                for (JsonNode courseId : data.get("courseIds")) {
                    CliPrinter.printBullet(courseId.asText());
                }
            }
        }

        menu.pressEnterToContinue();
    }

    private void handleSetSchedule() {
        CliPrinter.printTitle("Horaire d'un ensemble");

        String setId = menu.askForInput("ID de l'ensemble : ");
        if (setId.isEmpty()) return;

        ApiClient.ApiResponse response = apiClient.get("/sets/" + setId + "/schedule");

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        JsonNode data = response.getData();
        if (data != null && data.isArray()) {
            for (JsonNode course : data) {
                String courseId = course.has("id") ? course.get("id").asText() : "?";
                String name = course.has("name") ? course.get("name").asText() : "";
                
                System.out.println();
                System.out.println("  📚 " + courseId + " - " + name);
                
                if (course.has("schedules")) {
                    CliPrinter.printSchedule(course.get("schedules"), "");
                }
            }
        } else {
            CliPrinter.printInfo("Aucun horaire disponible.");
        }

        menu.pressEnterToContinue();
    }

    private void handleSetConflicts() {
        CliPrinter.printTitle("Détecter les conflits d'horaire (BONUS)");

        String setId = menu.askForInput("ID de l'ensemble : ");
        if (setId.isEmpty()) return;

        ApiClient.ApiResponse response = apiClient.get("/sets/" + setId + "/conflicts");

        if (!response.isSuccess()) {
            CliPrinter.printApiError(response);
            menu.pressEnterToContinue();
            return;
        }

        CliPrinter.printConflicts(response.getData());
        
        if (response.getMessage() != null) {
            System.out.println();
            CliPrinter.printInfo(response.getMessage());
        }

        menu.pressEnterToContinue();
    }
}
