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
        URI uri = HttpClientApi.buildUri(BASE_URL + "/" + cleanedId, params);

        try {
            Course course = clientApi.get(uri, Course.class);
            return (course == null) ? Optional.empty() : Optional.of(course);
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
        Optional<Course> opt = getCourseById(courseId);
        if (opt.isEmpty()) {
            return new EligibilityResult(false, List.of());
        }

        Course course = opt.get();
        List<String> prereqs = course.getPrerequisiteCourses();

        if (prereqs == null || prereqs.isEmpty()) {
            return new EligibilityResult(true, List.of());
        }

        List<String> completed = (completedCoursesIds == null) ? List.of() : completedCoursesIds;

        Set<String> done = new HashSet<>();
        for (String c : completed) {
            if (c != null && !c.isBlank()) {
                done.add(c.trim());
            }
        }

        List<String> missing = new ArrayList<>();
        for (String p : prereqs) {
            if (p != null && !p.isBlank()) {
                String normalized = p.trim();
                if (!done.contains(normalized)) {
                    missing.add(normalized);
                }
            }
        }

        boolean eligible = missing.isEmpty();
        return new EligibilityResult(eligible, missing);
    }
}
