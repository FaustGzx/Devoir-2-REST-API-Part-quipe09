package com.diro.ift2255.service;

import com.diro.ift2255.model.*;
import java.util.*;
/**
 * Service responsable de la comparaison avancée de cours.
 *
 * <p>Ce service combine les informations provenant du catalogue de cours,
 * des avis étudiants et des résultats académiques agrégés afin de produire
 * un résultat de comparaison global.</p>
 */

public class ComparisonService {

    private final CourseService courseService;
    private final ReviewService reviewService;
    private final AcademicResultService academicResultService;
     /**
     * Construit le service de comparaison des cours.
     *
     * @param courseService service d’accès aux informations de cours
     * @param reviewService service d’accès aux avis étudiants
     * @param academicResultService service d’accès aux résultats académiques agrégés
     */

    public ComparisonService(CourseService courseService, ReviewService reviewService, AcademicResultService academicResultService) {
        this.courseService = courseService;
        this.reviewService = reviewService;
        this.academicResultService = academicResultService;
    }
     /**
     * Compare plusieurs cours à partir de leurs identifiants.
     *
     * <p>La comparaison prend en compte les avis étudiants
     * (difficulté perçue et charge de travail) ainsi que
     * les statistiques académiques agrégées lorsqu’elles sont disponibles.</p>
     *
     * @param ids liste des identifiants des cours à comparer
     * @return résultat global de la comparaison des cours
     */

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
