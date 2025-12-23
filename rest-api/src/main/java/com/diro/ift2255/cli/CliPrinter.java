package com.diro.ift2255.cli;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;

/**
 * Classe utilitaire pour l'affichage formaté des réponses API dans le terminal.
 * Gère l'indentation, les tableaux, les messages d'erreur, etc.
 */
public class CliPrinter {

    private static final String SEPARATOR = "─".repeat(50);
    private static final String DOUBLE_SEP = "═".repeat(50);

    // ========================================================================
    // Titres et séparateurs
    // ========================================================================

    public static void printTitle(String title) {
        System.out.println();
        System.out.println(DOUBLE_SEP);
        System.out.println("  " + title);
        System.out.println(DOUBLE_SEP);
    }

    public static void printSubtitle(String subtitle) {
        System.out.println();
        System.out.println("── " + subtitle + " ──");
    }

    public static void printSeparator() {
        System.out.println(SEPARATOR);
    }

    // ========================================================================
    // Messages
    // ========================================================================

    public static void printSuccess(String message) {
        System.out.println("✅ " + message);
    }

    public static void printError(String message) {
        System.out.println("❌ " + message);
    }

    public static void printWarning(String message) {
        System.out.println("⚠️  " + message);
    }

    public static void printInfo(String message) {
        System.out.println("ℹ️  " + message);
    }

    public static void printApiError(ApiClient.ApiResponse response) {
        String msg = response.getMessage() != null ? response.getMessage() : "Erreur inconnue";
        if (response.getStatusCode() > 0) {
            printError("Erreur API (HTTP " + response.getStatusCode() + "): " + msg);
        } else {
            printError(msg);
        }
    }

    // ========================================================================
    // Affichage de données
    // ========================================================================

    public static void printField(String label, String value) {
        System.out.println("  " + label + " : " + (value != null ? value : "N/A"));
    }

    public static void printField(String label, Number value) {
        System.out.println("  " + label + " : " + (value != null ? value : "N/A"));
    }

    public static void printFieldIndent(String label, String value, int indent) {
        String prefix = "  ".repeat(indent);
        System.out.println(prefix + label + " : " + (value != null ? value : "N/A"));
    }

    public static void printBullet(String text) {
        System.out.println("  • " + text);
    }

    public static void printBulletIndent(String text, int indent) {
        String prefix = "  ".repeat(indent);
        System.out.println(prefix + "• " + text);
    }

    public static void printNumbered(int num, String text) {
        System.out.println("  " + num + ". " + text);
    }

    // ========================================================================
    // Affichage de cours
    // ========================================================================

    public static void printCourseShort(JsonNode course) {
        String id = getTextSafe(course, "id");
        String name = getTextSafe(course, "name");
        Double credits = course.has("credits") && !course.get("credits").isNull() 
                ? course.get("credits").asDouble() : null;
        
        System.out.print("  " + id + " - " + name);
        if (credits != null) {
            System.out.print(" (" + credits + " cr.)");
        }
        System.out.println();
    }

    public static void printCourseDetailed(JsonNode course) {
        String id = getTextSafe(course, "id");
        String name = getTextSafe(course, "name");
        String description = getTextSafe(course, "description");
        Double credits = course.has("credits") && !course.get("credits").isNull() 
                ? course.get("credits").asDouble() : null;

        System.out.println();
        System.out.println("  📚 " + id + " - " + name);
        printSeparator();

        if (description != null && !description.isBlank()) {
            // Tronquer si trop long
            if (description.length() > 300) {
                description = description.substring(0, 300) + "...";
            }
            System.out.println("  " + description);
            System.out.println();
        }

        printField("Crédits", credits);

        // Prérequis
        if (course.has("requirement_text") && !course.get("requirement_text").isNull()) {
            String prereq = course.get("requirement_text").asText();
            prereq = cleanPrereqText(prereq);
            printField("Prérequis", prereq);
        } else if (course.has("prerequisite_courses") && course.get("prerequisite_courses").isArray()) {
            StringBuilder prereqs = new StringBuilder();
            for (JsonNode p : course.get("prerequisite_courses")) {
                if (prereqs.length() > 0) prereqs.append(", ");
                prereqs.append(p.asText());
            }
            printField("Prérequis", prereqs.length() > 0 ? prereqs.toString() : "Aucun");
        }

        // Sessions offertes
        if (course.has("available_terms") && !course.get("available_terms").isNull()) {
            JsonNode terms = course.get("available_terms");
            StringBuilder sessions = new StringBuilder();
            if (terms.has("autumn") && terms.get("autumn").asBoolean()) sessions.append("Automne ");
            if (terms.has("winter") && terms.get("winter").asBoolean()) sessions.append("Hiver ");
            if (terms.has("summer") && terms.get("summer").asBoolean()) sessions.append("Été ");
            if (sessions.length() > 0) {
                printField("Sessions", sessions.toString().trim());
            }
        }

        // Cycle
        if (course.has("cycle") && !course.get("cycle").isNull()) {
            printField("Cycle", course.get("cycle").asText());
        }
    }

    // ========================================================================
    // Affichage d'horaire
    // ========================================================================

    public static void printSchedule(JsonNode schedules, String semester) {
        if (schedules == null || !schedules.isArray() || schedules.isEmpty()) {
            printInfo("Aucun horaire disponible pour " + semester);
            return;
        }

        printSubtitle("Horaire " + semester);

        for (JsonNode schedule : schedules) {
            if (!schedule.has("sections")) continue;
            
            for (JsonNode section : schedule.get("sections")) {
                String sectionName = getTextSafe(section, "name");
                System.out.println();
                System.out.println("  📍 Section " + sectionName);

                // Profs
                if (section.has("teachers") && section.get("teachers").isArray()) {
                    StringBuilder teachers = new StringBuilder();
                    for (JsonNode t : section.get("teachers")) {
                        if (teachers.length() > 0) teachers.append(", ");
                        teachers.append(t.asText());
                    }
                    if (teachers.length() > 0) {
                        printFieldIndent("Enseignant(s)", teachers.toString(), 2);
                    }
                }

                // Capacité
                if (section.has("capacity") && !section.get("capacity").isNull()) {
                    printFieldIndent("Capacité", section.get("capacity").asText(), 2);
                }

                // Activités (volets)
                if (section.has("volets") && section.get("volets").isArray()) {
                    for (JsonNode volet : section.get("volets")) {
                        String voletName = getTextSafe(volet, "name");
                        if (volet.has("activities") && volet.get("activities").isArray()) {
                            for (JsonNode activity : volet.get("activities")) {
                                printActivity(activity, voletName);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void printActivity(JsonNode activity, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("      ");
        
        // Type (TH, TP, LAB)
        if (type != null && !type.isBlank()) {
            sb.append("[").append(type).append("] ");
        }

        // Jours
        if (activity.has("days") && activity.get("days").isArray()) {
            for (JsonNode day : activity.get("days")) {
                sb.append(day.asText()).append(" ");
            }
        }

        // Heures
        String start = getTextSafe(activity, "start_time");
        String end = getTextSafe(activity, "end_time");
        if (start != null && end != null) {
            sb.append(start).append("-").append(end);
        }

        // Local
        String room = getTextSafe(activity, "room");
        if (room != null) {
            sb.append(" @ ").append(room);
        }

        System.out.println(sb);
    }

    // ========================================================================
    // Affichage des conflits (BONUS)
    // ========================================================================

    public static void printConflicts(JsonNode conflicts) {
        if (conflicts == null || !conflicts.isArray() || conflicts.isEmpty()) {
            printSuccess("Aucun conflit d'horaire détecté !");
            return;
        }

        printWarning(conflicts.size() + " conflit(s) détecté(s) :");
        System.out.println();

        int num = 1;
        for (JsonNode conflict : conflicts) {
            String desc = getTextSafe(conflict, "description");
            if (desc != null) {
                printNumbered(num++, desc);
            } else {
                // Fallback: construire description
                String c1 = getTextSafe(conflict, "course1");
                String c2 = getTextSafe(conflict, "course2");
                String day = getTextSafe(conflict, "day1");
                printNumbered(num++, c1 + " et " + c2 + " le " + day);
            }
        }
    }

    // ========================================================================
    // Affichage des avis
    // ========================================================================

    public static void printReviews(JsonNode reviews) {
        if (reviews == null || !reviews.isArray() || reviews.isEmpty()) {
            printInfo("Aucun avis pour ce cours.");
            return;
        }

        for (JsonNode review : reviews) {
            System.out.println();
            int difficulty = review.has("difficulty") ? review.get("difficulty").asInt() : 0;
            int workload = review.has("workload") ? review.get("workload").asInt() : 0;
            String comment = getTextSafe(review, "comment");

            System.out.println("  ⭐ Difficulté: " + difficulty + "/5 | Charge: " + workload + "/5");
            if (comment != null && !comment.isBlank()) {
                System.out.println("     \"" + comment + "\"");
            }
        }
    }

    public static void printReviewAggregate(JsonNode aggregate) {
        if (aggregate == null || aggregate.isNull()) return;

        int count = aggregate.has("count") ? aggregate.get("count").asInt() : 0;
        double avgDiff = aggregate.has("avgDifficulty") ? aggregate.get("avgDifficulty").asDouble() : 0;
        double avgWork = aggregate.has("avgWorkload") ? aggregate.get("avgWorkload").asDouble() : 0;

        System.out.println();
        printSubtitle("Résumé des avis (" + count + " avis)");
        printField("Difficulté moyenne", String.format("%.1f/5", avgDiff));
        printField("Charge moyenne", String.format("%.1f/5", avgWork));
    }

    // ========================================================================
    // Affichage éligibilité
    // ========================================================================

    public static void printEligibility(JsonNode result) {
        if (result == null) return;

        boolean eligible = result.has("eligible") && result.get("eligible").asBoolean();
        
        System.out.println();
        if (eligible) {
            printSuccess("Vous êtes ÉLIGIBLE à ce cours !");
        } else {
            printError("Vous n'êtes PAS éligible à ce cours.");
        }

        // Message cycle
        if (result.has("cycleMessage") && !result.get("cycleMessage").isNull()) {
            System.out.println("  " + result.get("cycleMessage").asText());
        }

        // Prérequis manquants
        if (result.has("missingPrerequisites") && result.get("missingPrerequisites").isArray()) {
            JsonNode missing = result.get("missingPrerequisites");
            if (!missing.isEmpty()) {
                System.out.println();
                printWarning("Prérequis manquants :");
                for (JsonNode m : missing) {
                    printBullet(m.asText());
                }
            }
        }
    }

    // ========================================================================
    // Affichage résultats académiques
    // ========================================================================

    public static void printAcademicResults(JsonNode results) {
        if (results == null || results.isNull()) {
            printInfo("Aucun résultat académique disponible pour ce cours.");
            return;
        }

        printSubtitle("Résultats académiques");

        if (results.has("average") && !results.get("average").isNull()) {
            printField("Moyenne", String.format("%.2f", results.get("average").asDouble()));
        }
        if (results.has("median") && !results.get("median").isNull()) {
            printField("Médiane", String.format("%.2f", results.get("median").asDouble()));
        }
        if (results.has("ecartType") && !results.get("ecartType").isNull()) {
            printField("Écart-type", String.format("%.2f", results.get("ecartType").asDouble()));
        }
        if (results.has("participants") && !results.get("participants").isNull()) {
            printField("Participants", results.get("participants").asInt());
        }
        if (results.has("passingRate") && !results.get("passingRate").isNull()) {
            printField("Taux de réussite", String.format("%.1f%%", results.get("passingRate").asDouble() * 100));
        }
    }

    // ========================================================================
    // Affichage comparaison
    // ========================================================================

    public static void printComparison(JsonNode comparison) {
        if (comparison == null || comparison.isNull()) return;

        // Cours comparés
        if (comparison.has("courses") && comparison.get("courses").isArray()) {
            printSubtitle("Cours comparés");
            for (JsonNode course : comparison.get("courses")) {
                printCourseShort(course);
            }
        }

        // Résumé
        if (comparison.has("summary")) {
            JsonNode summary = comparison.get("summary");
            System.out.println();
            printSubtitle("Comparaison");
            
            if (summary.has("totalCredits")) {
                printField("Total crédits", summary.get("totalCredits").asDouble());
            }
            if (summary.has("estimatedWorkload")) {
                printField("Charge estimée", summary.get("estimatedWorkload").asText());
            }
        }

        // Critères détaillés
        if (comparison.has("criteria") && comparison.get("criteria").isArray()) {
            System.out.println();
            printSubtitle("Détails par critère");
            for (JsonNode criterion : comparison.get("criteria")) {
                String name = getTextSafe(criterion, "name");
                String value = getTextSafe(criterion, "value");
                if (name != null) {
                    printField(name, value);
                }
            }
        }
    }

    // ========================================================================
    // Utilitaires
    // ========================================================================

    private static String getTextSafe(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private static String cleanPrereqText(String text) {
        if (text == null) return null;
        String cleaned = text.trim();
        String lower = cleaned.toLowerCase();
        int idx = lower.indexOf("prerequisite_courses");
        if (idx != -1) {
            int colonIdx = cleaned.indexOf(':', idx);
            if (colonIdx != -1 && colonIdx + 1 < cleaned.length()) {
                cleaned = cleaned.substring(colonIdx + 1).trim();
            }
        }
        return cleaned;
    }

    public static void printEmptyLine() {
        System.out.println();
    }
}
