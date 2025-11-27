package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.EligibilityResult;
import com.diro.ift2255.util.HttpClientApi;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.util.*;

public class CourseService {
    private final HttpClientApi clientApi;
    private static final String BASE_URL = "https://planifium-api.onrender.com/api/v1/courses";

    public CourseService(HttpClientApi clientApi) {
        this.clientApi = clientApi;
    }

    /**
     * Récupère une liste de cours depuis Planifium.
     * On passe les query params tels quels (sigle, name, description, etc.).
     */
    public List<Course> getAllCourses(Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;

        URI uri = HttpClientApi.buildUri(BASE_URL, params);
        return clientApi.get(uri, new TypeReference<List<Course>>() {});
    }

    /**
     * Raccourci pour récupérer un cours par id sans paramètres en plus.
     */
    public Optional<Course> getCourseById(String courseId) {
        return getCourseById(courseId, null);
    }

    /**
     * Récupère un cours par id avec des paramètres optionnels
     * (ex: include_schedule, schedule_semester).
     */
    public Optional<Course> getCourseById(String courseId, Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;
        URI uri = HttpClientApi.buildUri(BASE_URL + "/" + courseId, params);

        try {
            Course course = clientApi.get(uri, Course.class);
            return Optional.of(course);
        } catch (RuntimeException e) {
            // Si l'API Planifium ne trouve rien ou plante, on renvoie juste un Optional vide
            return Optional.empty();
        }
    }

    /**
     * Compare des cours (version simple, pour rester compatible avec l'ancien code).
     * Si tu ne veux pas gérer de params en plus, tu peux appeler juste celle-là.
     */
    public List<Course> compareCourses(List<String> courseIds) {
        return compareCourses(courseIds, null);
    }

    /**
     * Compare des cours, en laissant la possibilité de passer des query params
     * (ex: include_schedule=true, schedule_semester=A25).
     *
     * Pour l'instant, "comparer" veut dire : récupérer les infos complètes
     * de chaque cours, et laisser l'interface (CLI, UI) décider comment les afficher.
     */
    public List<Course> compareCourses(List<String> courseIds, Map<String, String> queryParams) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        Map<String, String> params = (queryParams == null)
                ? Collections.emptyMap()
                : queryParams;

        List<Course> result = new ArrayList<>();

        for (String id : courseIds) {
            if (id == null || id.isBlank()) {
                continue;
            }

            // On utilise la version avec params pour pouvoir aller chercher les horaires si on veut
            getCourseById(id.trim(), params).ifPresent(result::add);
        }

        return result;
    }

    // ========================================================================
    // Éligibilité locale à un cours
    // ========================================================================

    /**
     * Vérifie si un étudiant est éligible à un cours donné, en fonction
     * des cours qu'il a déjà réussis.
     *
     * Idée simple :
     *  - On va chercher le cours sur Planifium (getCourseById).
     *  - On lit sa liste de prérequis (prerequisiteCourses).
     *  - On regarde si la liste "completedCoursesIds" contient tous ces prérequis.
     *
     * Exemple d'utilisation plus tard dans la CLI :
     *  - courseId = "IFT2255"
     *  - completedCoursesIds = ["IFT1015", "IFT1025"]
     */
    public EligibilityResult checkEligibility(String courseId, List<String> completedCoursesIds) {
        // On récupère d'abord le cours, pour avoir accès à ses prérequis
        Optional<Course> opt = getCourseById(courseId);

        // Si le cours n'existe pas ou n'est pas trouvé, on considère qu'on ne peut pas s'inscrire
        if (opt.isEmpty()) {
            return new EligibilityResult(false, List.of());
        }

        Course course = opt.get();
        List<String> prereqs = course.getPrerequisiteCourses();

        // Pas de prérequis → éligible d'office
        if (prereqs == null || prereqs.isEmpty()) {
            return new EligibilityResult(true, List.of());
        }

        // On nettoie un peu la liste des cours déjà complétés (null, espaces, etc.)
        List<String> completed = (completedCoursesIds == null) ? List.of() : completedCoursesIds;

        Set<String> done = new HashSet<>();
        for (String c : completed) {
            if (c != null && !c.isBlank()) {
                done.add(c.trim());
            }
        }

        // On regarde quels prérequis manquent
        List<String> missing = new ArrayList<>();

        for (String p : prereqs) {
            if (p == null || p.isBlank()) {
                continue;
            }
            String normalized = p.trim();
            if (!done.contains(normalized)) {
                missing.add(normalized);
            }
        }

        boolean eligible = missing.isEmpty();
        return new EligibilityResult(eligible, missing);
    }
}
