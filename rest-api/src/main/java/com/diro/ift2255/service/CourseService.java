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

    // ------------------------------
    // Recherche de cours
    // ------------------------------
    public List<Course> getAllCourses(Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;
        URI uri = HttpClientApi.buildUri(BASE_URL, params);
        return clientApi.get(uri, new TypeReference<List<Course>>() {});
    }

    // ------------------------------
    // Récupération d’un cours
    // ------------------------------
    public Optional<Course> getCourseById(String courseId) {
        return getCourseById(courseId, null);
    }

    public Optional<Course> getCourseById(String courseId, Map<String, String> queryParams) {
        if (courseId == null || courseId.isBlank()) {
            return Optional.empty();
        }

        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;

        String cleanedId = courseId.trim();
        URI uri = HttpClientApi.buildUri(BASE_URL + "/" + cleanedId.toLowerCase(), params);


        try {
            Course course = clientApi.get(uri, Course.class);
            return Optional.ofNullable(course);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    // ------------------------------
    // Comparaison de plusieurs cours
    // ------------------------------
    public List<Course> compareCourses(List<String> courseIds) {
        return compareCourses(courseIds, null);
    }

    public List<Course> compareCourses(List<String> courseIds, Map<String, String> queryParams) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;
        List<Course> result = new ArrayList<>();

        for (String id : courseIds) {
            if (id != null && !id.isBlank()) {
                getCourseById(id.trim(), params).ifPresent(result::add);
            }
        }

        return result;
    }

    // ------------------------------
    // Vérification d’éligibilité
    // ------------------------------
    public EligibilityResult checkEligibility(String courseId, List<String> completedCoursesIds) {
        return checkEligibility(courseId, completedCoursesIds, null);
    }

    public EligibilityResult checkEligibility(String courseId, List<String> completedCoursesIds, Integer cycle) {
    Optional<Course> opt = getCourseById(courseId);
    if (opt.isEmpty()) {
        return new EligibilityResult(false, List.of(), false, null);
    }

    // 1) Prérequis
    Course course = opt.get();
    List<String> prereqs = course.getPrerequisiteCourses();

    Set<String> done = new HashSet<>();
    for (String c : (completedCoursesIds == null ? List.<String>of() : completedCoursesIds)) {
        if (c != null && !c.isBlank()) done.add(c.trim().toUpperCase());
    }

    List<String> missing = new ArrayList<>();
    if (prereqs != null) {
        for (String p : prereqs) {
            if (p != null && !p.isBlank()) {
                String normalized = p.trim().toUpperCase();
                if (!done.contains(normalized)) missing.add(normalized);
            }
        }
    }

    boolean prereqOk = missing.isEmpty();

    // 2) Cycle (règle minimale)
    Integer required = inferRequiredCycle(courseId);
    boolean cycleOk = true;

    if (cycle != null) {
        if (cycle < 1 || cycle > 3) {
            // cycle invalide -> on refuse
            return new EligibilityResult(false, missing, false, required);
        }
        cycleOk = (required == null) || (cycle >= required);
    }

    boolean eligible = prereqOk && cycleOk;
    return new EligibilityResult(eligible, missing, cycleOk, required);
}

// Règle minimale: codes >= 6000 -> cycle 2/3 (ici on met 2)
private Integer inferRequiredCycle(String courseId) {
    if (courseId == null) return null;
    String id = courseId.trim().toUpperCase();
    if (!id.matches("^[A-Z]{3}\\d{4}$")) return null;

    int num = Integer.parseInt(id.substring(3));
    if (num >= 6000) return 2; // cycles supérieurs
    return 1;                  // 1er cycle
}

    public List<Course> searchBySiglePrefix(String prefix, Map<String, String> queryParams) {
    // Stratégie: utiliser la recherche Planifium "name/description" n'aide pas.
    // On prend une liste de cours "reg" via un endpoint déjà filtré si vous avez une source locale,
    // sinon on fait une requête Planifium en récupérant une liste raisonnable et on filtre.
    // Version minimale: réutiliser getAllCourses + filtrer côté serveur.

    List<Course> all = getAllCourses(queryParams);
    if (all == null) return List.of();

    return all.stream()
            .filter(c -> c != null && c.getId() != null && c.getId().toUpperCase().startsWith(prefix))
            .toList();
}

}
