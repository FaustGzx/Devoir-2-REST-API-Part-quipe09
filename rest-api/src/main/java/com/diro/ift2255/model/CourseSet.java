package com.diro.ift2255.model;

import java.util.List;
/**
 * Représente un ensemble de cours sélectionnés par un étudiant
 * pour un trimestre donné.
 *
 * <p>Un {@code CourseSet} contient un identifiant unique, le trimestre visé
 * ainsi que la liste des cours inclus dans l’ensemble.</p>
 *
 * <p>Cette structure est utilisée pour générer un horaire global
 * et détecter des conflits éventuels entre les cours.</p>
 */
public class CourseSet {
    private String id;            // identifiant de l’ensemble
    private String semester;      // H25/A24/E24
    private List<String> courseIds;

    public CourseSet() {}

    public CourseSet(String id, String semester, List<String> courseIds) {
        this.id = id;
        this.semester = semester;
        this.courseIds = courseIds;
    }

    public String getId() { return id; }
    public String getSemester() { return semester; }
    public List<String> getCourseIds() { return courseIds; }

    public void setId(String id) { this.id = id; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setCourseIds(List<String> courseIds) { this.courseIds = courseIds; }
}
