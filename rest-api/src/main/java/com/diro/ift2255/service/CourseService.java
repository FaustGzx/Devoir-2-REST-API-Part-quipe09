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
        // SAFE: On utilise une liste locale de sigles connus par préfixe
        // pour éviter d'appeler Planifium "GET all courses" (très lourd)
        
        if (prefix == null || prefix.isBlank()) return List.of();
        String normalizedPrefix = prefix.trim().toUpperCase();

        // Liste de sigles connus par département (extensible)
        Map<String, List<String>> siglesByPrefix = Map.of(
            "IFT", List.of(
                "IFT1005", "IFT1015", "IFT1016", "IFT1025", "IFT1065", "IFT1215", "IFT1227",
                "IFT2015", "IFT2035", "IFT2105", "IFT2125", "IFT2255", "IFT2505", "IFT2905",
                "IFT3150", "IFT3205", "IFT3225", "IFT3245", "IFT3275", "IFT3295", "IFT3325", "IFT3355", "IFT3395", "IFT3700", "IFT3710",
                "IFT6135", "IFT6232", "IFT6269", "IFT6390", "IFT6561", "IFT6758", "IFT6760"
            ),
            "MAT", List.of(
                "MAT1000", "MAT1101", "MAT1400", "MAT1410", "MAT1600", "MAT1620", "MAT1720",
                "MAT2050", "MAT2450", "MAT2717"
            ),
            "STT", List.of(
                "STT1000", "STT1682", "STT1700", "STT2000", "STT2700"
            ),
            "PHY", List.of(
                "PHY1234", "PHY1441", "PHY1620", "PHY1652"
            )
        );

        // Récupérer la liste de sigles pour ce préfixe
        List<String> sigles = siglesByPrefix.get(normalizedPrefix);
        if (sigles == null || sigles.isEmpty()) {
            // Préfixe inconnu: retourner liste vide plutôt que charger tous les cours
            return List.of();
        }

        // Interroger Planifium pour ces sigles spécifiques (safe et rapide)
        List<Course> result = new ArrayList<>();
        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;

        for (String sigle : sigles) {
            getCourseById(sigle, params).ifPresent(result::add);
        }

        return result;
    }

    /**
     * Récupère les cours offerts pour un trimestre donné (global, sans filtre de programme).
     * Utilise une liste de sigles courants du DIRO pour interroger Planifium.
     */
    public List<Course> getCoursesOfferedBySemester(String semester, int limit) {
        if (semester == null || semester.isBlank()) return List.of();

        String sem = semester.trim().toLowerCase();

        // Liste de sigles courants du DIRO 
        List<String> commonSigles = List.of(
                "IFT1015", "IFT1025", "IFT1065", "IFT1215", "IFT1227",
                "IFT2015", "IFT2035", "IFT2105", "IFT2125", "IFT2255", "IFT2505", "IFT2905",
                "IFT3150", "IFT3205", "IFT3225", "IFT3245", "IFT3275", "IFT3295", "IFT3325", "IFT3355", "IFT3395", "IFT3700", "IFT3710",
                "IFT6135", "IFT6390", "IFT6561", "IFT6758", "IFT6760"
        );

        Map<String, String> qp = new HashMap<>();
        qp.put("include_schedule", "true");
        qp.put("schedule_semester", sem);

        List<Course> offered = new ArrayList<>();
        int count = 0;

        for (String sigle : commonSigles) {
            if (count >= limit) break;

            Optional<Course> opt = getCourseById(sigle, qp);
            if (opt.isPresent()) {
                Course course = opt.get();
                // Vérifier que le cours a bien un horaire pour ce trimestre
                if (course.getSchedules() != null && !course.getSchedules().isEmpty()) {
                    offered.add(course);
                    count++;
                }
            }
        }

        return offered;
    }

}
