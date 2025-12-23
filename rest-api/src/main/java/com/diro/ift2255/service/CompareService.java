package com.diro.ift2255.service;

import com.diro.ift2255.model.*;

import java.util.*;

public class CompareService {

    private final CourseService courseService;
    private final ReviewService reviewService;
    private final AcademicResultService academicResultService;

    public CompareService(CourseService courseService,
                          ReviewService reviewService,
                          AcademicResultService academicResultService) {
        this.courseService = courseService;
        this.reviewService = reviewService;
        this.academicResultService = academicResultService;
    }

    public List<CompareItem> compare(List<String> ids) {
        if (ids == null) return List.of();

        List<CompareItem> out = new ArrayList<>();
        for (String raw : ids) {
            if (raw == null || raw.isBlank()) continue;
            String id = raw.trim().toUpperCase();

            // Catalogue
            String name = courseService.getCourseById(id)
                    .map(Course::getName)
                    .orElse(id);

            // Avis
            ReviewAggregate agg = reviewService.getAggregateForCourse(id);

            // CSV
            var arOpt = academicResultService.getBySigle(id);
            Double score = arOpt.map(AcademicResult::getScore).orElse(null);
            Integer participants = arOpt.map(AcademicResult::getParticipants).orElse(null);
            String moyenne = arOpt.map(AcademicResult::getMoyenne).orElse(null);

            out.add(new CompareItem(
                    id, name,
                    agg.getCount(), agg.getAvgDifficulty(), agg.getAvgWorkload(),
                    score, participants, moyenne
            ));
        }
        return out;
    }
}
