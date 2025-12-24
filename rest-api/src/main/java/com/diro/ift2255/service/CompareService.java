package com.diro.ift2255.service;

import com.diro.ift2255.model.*;

import java.util.*;
/**
 * Service responsable de la comparaison de plusieurs cours.
 *
 * <p>Ce service agrège différentes sources de données (cours,
 * avis étudiants, résultats académiques) afin de produire
 * des indicateurs comparatifs tels que la difficulté,
 * la charge de travail et les statistiques globales.</p>
 */
public class CompareService {

    private final CourseService courseService;
    private final ReviewService reviewService;
    private final AcademicResultService academicResultService;
    /**
    * Service responsable de la comparaison de plusieurs cours.
    *
    * <p>Ce service agrège différentes sources de données (cours,
    * avis étudiants, résultats académiques) afin de produire
    * des indicateurs comparatifs tels que la difficulté,
    * la charge de travail et les statistiques globales.</p>
    */
    public CompareService(CourseService courseService,
                          ReviewService reviewService,
                          AcademicResultService academicResultService) {
        this.courseService = courseService;
        this.reviewService = reviewService;
        this.academicResultService = academicResultService;
    }
        /**
     * Compare plusieurs cours à partir de leurs identifiants.
     *
     * <p>La comparaison est basée sur les avis étudiants pour estimer
     * la charge de travail et la difficulté, ainsi que sur les résultats
     * académiques agrégés lorsque disponibles.</p>
     *
     * @param ids liste des identifiants des cours à comparer
     * @return liste d’objets {@link CompareItem} représentant les résultats comparatifs
     */
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
