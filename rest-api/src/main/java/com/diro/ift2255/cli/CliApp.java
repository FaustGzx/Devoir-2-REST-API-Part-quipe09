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
    // Index local (session CLI) : numéro lisible -> UUID d'ensemble
    private final LinkedHashMap<Integer, String> setIndex = new LinkedHashMap<>();
    /**
    * Construit l’application CLI en configurant l’URL de base du serveur REST.
    *
    * @param baseUrl URL de base du serveur REST (ex : "http://localhost:7000")
    */
    public CliApp(String baseUrl) {
        this.scanner = new Scanner(System.in);
        this.apiClient = new ApiClient(baseUrl);
        this.menu = new CliMenu(scanner);
        this.running = true;
    }

    /**
    * Point d’entrée principal du programme CLI.
    *
    * @param args arguments de la ligne de commande (ex : "--baseUrl=...")
    */
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
    * Lance la boucle principale du CLI et gère l’interaction avec l’utilisateur.
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
        System.out.println("  [COURSE] " + sigle + " - " + name);

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

        // Afficher les CompareItem (inclut avis et résultats agrégés)
        if (data != null && data.isArray()) {
            CliPrinter.printSubtitle("Cours comparés");
            CliPrinter.printCompareItems(data);
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
            case "5" -> handleCompareSets();
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

            // Enregistrer un numéro lisible pour cet ensemble dans la session
            int num = setIndex.size() + 1;
            if (setId != null && !"?".equals(setId)) {
                setIndex.put(num, setId);
            }

            CliPrinter.printSuccess("Ensemble #" + num + " créé pour " + semester);
            CliPrinter.printField("UUID interne", setId);
            CliPrinter.printInfo("Vous pouvez utiliser soit le numéro (ex: 1) soit le UUID complet.");
        } else {
            CliPrinter.printApiError(response);
        }

        menu.pressEnterToContinue();
    }

    private void handleViewSet() {
        CliPrinter.printTitle("Voir un ensemble de cours");

        String raw = menu.askForInput("Identifiant de l'ensemble (#num ou UUID) : ");
        String setId = resolveSetId(raw);
        if (setId == null) {
            CliPrinter.printError("Identifiant d'ensemble inconnu. Utilisez un UUID ou un numéro créé dans cette session.");
            return;
        }

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

        String raw = menu.askForInput("Identifiant de l'ensemble (#num ou UUID) : ");
        String setId = resolveSetId(raw);
        if (setId == null) {
            CliPrinter.printError("Identifiant d'ensemble inconnu. Utilisez un UUID ou un numéro créé dans cette session.");
            return;
        }

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
                System.out.println("  [COURSE] " + courseId + " - " + name);
                
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

        String raw = menu.askForInput("Identifiant de l'ensemble (#num ou UUID) : ");
        String setId = resolveSetId(raw);
        if (setId == null) {
            CliPrinter.printError("Identifiant d'ensemble inconnu. Utilisez un UUID ou un numéro créé dans cette session.");
            return;
        }

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

    // ========================================================================
    // Comparer deux ensembles (A vs B)
    // ========================================================================

    private void handleCompareSets() {
        CliPrinter.printTitle("Comparer deux ensembles (A vs B)");

        String rawA = menu.askForInput("Ensemble A (#num ou UUID) : ");
        String rawB = menu.askForInput("Ensemble B (#num ou UUID) : ");

        String idA = resolveSetId(rawA);
        String idB = resolveSetId(rawB);

        if (idA == null || idB == null) {
            CliPrinter.printError("Identifiants invalides. Utilisez des UUID ou des numéros connus.");
            return;
        }

        ApiClient.ApiResponse respA = apiClient.get("/sets/" + idA + "/schedule");
        ApiClient.ApiResponse respB = apiClient.get("/sets/" + idB + "/schedule");

        if (!respA.isSuccess()) {
            CliPrinter.printApiError(respA);
            menu.pressEnterToContinue();
            return;
        }
        if (!respB.isSuccess()) {
            CliPrinter.printApiError(respB);
            menu.pressEnterToContinue();
            return;
        }

        // Libellés lisibles (numéro si connu)
        String labelA = buildSetLabel(idA);
        String labelB = buildSetLabel(idB);

        printSideBySideSchedules(respA.getData(), labelA, respB.getData(), labelB);

        // Petits résumés
        double creditsA = sumCredits(respA.getData());
        double creditsB = sumCredits(respB.getData());
        int countA = countCourses(respA.getData());
        int countB = countCourses(respB.getData());

        System.out.println();
        CliPrinter.printSeparator();
        CliPrinter.printField("Crédits (A)", creditsA);
        CliPrinter.printField("Crédits (B)", creditsB);
        CliPrinter.printField("Cours (A)", countA);
        CliPrinter.printField("Cours (B)", countB);

        menu.pressEnterToContinue();
    }

    private String buildSetLabel(String uuid) {
        for (Map.Entry<Integer, String> e : setIndex.entrySet()) {
            if (e.getValue().equals(uuid)) {
                return "Ensemble #" + e.getKey();
            }
        }
        // Fallback: abréger l'UUID
        return uuid.length() >= 8 ? ("UUID " + uuid.substring(0, 8)) : uuid;
    }

    private double sumCredits(JsonNode scheduleData) {
        if (scheduleData == null || !scheduleData.isArray()) return 0.0;
        double total = 0.0;
        for (JsonNode course : scheduleData) {
            if (course.has("credits") && !course.get("credits").isNull()) {
                total += course.get("credits").asDouble();
            }
        }
        return total;
    }

    private int countCourses(JsonNode scheduleData) {
        if (scheduleData == null || !scheduleData.isArray()) return 0;
        return scheduleData.size();
    }

    private void printSideBySideSchedules(JsonNode dataA, String labelA, JsonNode dataB, String labelB) {
        CliPrinter.printSubtitle(labelA + "  vs  " + labelB);

        // Construire les lignes pour A et B (cours + premier créneau)
        List<String> linesA = buildCourseLines(dataA);
        List<String> linesB = buildCourseLines(dataB);

        int max = Math.max(linesA.size(), linesB.size());
        int width = 50; // largeur colonne

        System.out.println();
        System.out.printf("%-" + width + "s %s%n", "CHEMIN A (" + labelA + ")", "CHEMIN B (" + labelB + ")");
        System.out.println("-".repeat(width) + " " + "-".repeat(width));

        for (int i = 0; i < max; i++) {
            String left = i < linesA.size() ? linesA.get(i) : "";
            String right = i < linesB.size() ? linesB.get(i) : "";
            if (left.length() > width) left = left.substring(0, width - 3) + "...";
            if (right.length() > width) right = right.substring(0, width - 3) + "...";
            System.out.printf("%-" + width + "s %s%n", left, right);
        }
    }

    private List<String> buildCourseLines(JsonNode data) {
        List<String> out = new ArrayList<>();
        if (data == null || !data.isArray()) return out;

        for (JsonNode course : data) {
            String id = course.has("id") ? course.get("id").asText() : "?";
            String name = course.has("name") ? course.get("name").asText() : "";
            String slot = firstSlot(course);
            String line = String.format("%s  %s%s", id, name, (slot != null ? "  " + slot : ""));
            out.add(line);
        }
        return out;
    }

    private String firstSlot(JsonNode course) {
        if (course == null || !course.has("schedules")) return null;
        JsonNode schedules = course.get("schedules");
        if (!schedules.isArray()) return null;
        for (JsonNode schedule : schedules) {
            if (!schedule.has("sections")) continue;
            JsonNode sections = schedule.get("sections");
            if (!sections.isArray() || sections.isEmpty()) continue;
            JsonNode section = sections.get(0);
            if (section.has("volets") && section.get("volets").isArray()) {
                for (JsonNode volet : section.get("volets")) {
                    if (volet.has("activities") && volet.get("activities").isArray()) {
                        for (JsonNode activity : volet.get("activities")) {
                            StringBuilder sb = new StringBuilder();
                            if (activity.has("days") && activity.get("days").isArray() && activity.get("days").size() > 0) {
                                sb.append(activity.get("days").get(0).asText()).append(" ");
                            }
                            String start = activity.has("start_time") ? activity.get("start_time").asText() : null;
                            String end = activity.has("end_time") ? activity.get("end_time").asText() : null;
                            if (start != null && end != null) {
                                sb.append(start).append("–").append(end);
                            }
                            return sb.length() > 0 ? sb.toString() : null;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Resolve un identifiant d'ensemble fourni par l'utilisateur.
     * Accepte : "1", "#1", "Ensemble #1" ou un UUID.
     */
    private String resolveSetId(String input) {
        if (input == null) return null;
        String in = input.trim();

        // Numéro au format simple
        if (in.matches("^(?i)(ensemble\s*)?#?\\d+$")) {
            String digits = in.replaceAll("[^0-9]", "");
            try {
                int num = Integer.parseInt(digits);
                return setIndex.getOrDefault(num, null);
            } catch (NumberFormatException ignored) { }
        }

        // UUID (format standard)
        if (in.matches("(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return in.toLowerCase();
        }
        return null;
    }
}
