package com.diro.ift2255.model;

import java.util.List;
/**
 * Représente le résultat global d’une comparaison entre plusieurs cours.
 *
 * <p>Contient la liste des éléments de comparaison détaillés
 * ainsi que des informations synthétiques utilisées par le frontend/CLI.</p>
 */
public class CourseComparisonResult {
    private List<CourseComparisonItem> items;

    public CourseComparisonResult(List<CourseComparisonItem> items) {
        this.items = items;
    }

    public List<CourseComparisonItem> getItems() { return items; }
}

