package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.CourseSet;

import java.util.*;

public class CourseSetService {

    private final CourseService courseService;
    private final Map<String, CourseSet> sets = new HashMap<>();

    public CourseSetService(CourseService courseService) {
        this.courseService = courseService;
    }

    public Optional<CourseSet> createSet(String semester, List<String> courseIds) {
        String sem = normalizeSemester(semester);
        if (sem == null) return Optional.empty();

        if (courseIds == null) return Optional.empty();
        List<String> cleaned = courseIds.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (cleaned.isEmpty() || cleaned.size() > 6) return Optional.empty();
        for (String id : cleaned) {
            if (!id.matches("^[A-Z]{3}\\d{4}$")) return Optional.empty();
        }

        String id = UUID.randomUUID().toString();
        CourseSet set = new CourseSet(id, sem, cleaned);
        sets.put(id, set);
        return Optional.of(set);
    }

    public Optional<CourseSet> getSet(String id) {
        return Optional.ofNullable(sets.get(id));
    }

    // Horaire résultant: retourne les cours avec schedules filtrés par trimestre
    public List<Course> getSetSchedule(String setId) {
        Optional<CourseSet> opt = getSet(setId);
        if (opt.isEmpty()) return List.of();

        CourseSet set = opt.get();
        Map<String, String> qp = new HashMap<>();
        qp.put("include_schedule", "true");
        qp.put("schedule_semester", set.getSemester().toLowerCase());

        List<Course> out = new ArrayList<>();
        for (String cid : set.getCourseIds()) {
            courseService.getCourseById(cid, qp).ifPresent(out::add);
        }
        return out;
    }

    private String normalizeSemester(String s) {
        if (s == null) return null;
        String sem = s.trim().toUpperCase();
        if (!sem.matches("^[HAE]\\d{2}$")) return null;
        return sem;
    }
}
