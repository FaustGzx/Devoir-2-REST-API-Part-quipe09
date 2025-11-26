package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
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

    /** Fetch all courses */
    public List<Course> getAllCourses(Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;

        URI uri = HttpClientApi.buildUri(BASE_URL, params);
        List<Course> courses = clientApi.get(uri, new TypeReference<List<Course>>() {});

        return courses;
    }

    /** Fetch a course by ID */
    public Optional<Course> getCourseById(String courseId) {
        return getCourseById(courseId, null);
    }

    /** Fetch a course by ID with optional query params */
    public Optional<Course> getCourseById(String courseId, Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;
        URI uri = HttpClientApi.buildUri(BASE_URL + "/" + courseId, params);

        try {
            Course course = clientApi.get(uri, Course.class);
            return Optional.of(course);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Comparer des cours - version simple (compatibilité)
     * @param courseIds Liste d'ID de cours à comparer
     * @return Liste des cours correspondants
     */
    public List<Course> compareCourses(List<String> courseIds) {
        return compareCourses(courseIds, null);
    }

    /**
     * Comparer des cours - version avec paramètres (ex: include_schedule, schedule_semester).
     * @param courseIds   Liste d'ID de cours à comparer
     * @param queryParams Paramètres supplémentaires à transmettre à l'API Planifium
     * @return Liste des cours correspondants
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

            // On utilise la version de getCourseById qui accepte aussi les query params
            getCourseById(id.trim(), params).ifPresent(result::add);
        }

        return result;
    }
}
