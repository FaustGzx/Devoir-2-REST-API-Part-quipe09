package com.diro.ift2255.service;

import com.diro.ift2255.model.*;
import java.util.*;

public class ComparisonService {

    private final CourseService courseService;
    private final ReviewService reviewService;
    private final AcademicResultService academicResultService;

    public ComparisonService(CourseService courseService, ReviewService reviewService, AcademicResultService academicResultService) {
        this.courseService = courseService;
        this.reviewService = reviewService;
        this.academicResultService = academicResultService;
    }

    public CourseComparisonResult compare(List<String> ids) {
        List<CourseComparisonItem> items = new ArrayList<>();

        for (String id : ids) {
            String cid = id.trim().toUpperCase();

            var courseOpt = courseService.getCourseById(cid);
            String name = courseOpt.map(Course::getName).orElse(cid);

            ReviewAggregate agg = reviewService.getAggregateForCourse(cid);
            double avgWorkload = agg.getAvgWorkload();
            double avgDifficulty = agg.getAvgDifficulty();

            Double score = academicResultService.getBySigle(cid).map(AcademicResult::getScore).orElse(null);

            items.add(new CourseComparisonItem(cid, name, avgWorkload, avgDifficulty, score));
        }

        return new CourseComparisonResult(items);
    }
}
