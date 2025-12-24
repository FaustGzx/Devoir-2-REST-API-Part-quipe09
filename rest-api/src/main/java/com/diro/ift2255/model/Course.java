// src/main/java/com/diro/ift2255/model/Course.java
package com.diro.ift2255.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Représente un cours universitaire du catalogue officiel.
 *
 * <p>Un {@code Course} contient les informations générales d’un cours
 * (sigle, titre, crédits, description, préalables, etc.)
 * ainsi que ses horaires par trimestre.</p>
 *
 * <p>Les structures internes {@link Course.Schedule} et
 * {@link Course.Section} sont utilisées pour représenter l’horaire détaillé.</p>
 */
public class Course {

    private String id;
    private String name;
    private String description;

    @JsonProperty("prerequisite_courses")
    private List<String> prerequisiteCourses;

    private Double credits;

    @JsonProperty("requirement_text")
    private String requirementText;

    @JsonProperty("available_terms")
    private Map<String, Boolean> availableTerms;

    @JsonProperty("available_periods")
    private Map<String, Boolean> availablePeriods;

    private List<Schedule> schedules;
    /**
    * Construit un {@code Course} vide.
    *
    * <p>Utilisé pour la désérialisation JSON.</p>
    */
    public Course() {}
    /**
    * Construit un cours avec ses informations principales.
    *
    * @param name nom du cours
    * @param description description du cours
    */
    public Course(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getPrerequisiteCourses() { return prerequisiteCourses; }
    public void setPrerequisiteCourses(List<String> prerequisiteCourses) { this.prerequisiteCourses = prerequisiteCourses; }

    public Double getCredits() { return credits; }
    public void setCredits(Double credits) { this.credits = credits; }

    public String getRequirementText() { return requirementText; }
    public void setRequirementText(String requirementText) { this.requirementText = requirementText; }

    public Map<String, Boolean> getAvailableTerms() { return availableTerms; }
    public void setAvailableTerms(Map<String, Boolean> availableTerms) { this.availableTerms = availableTerms; }

    public Map<String, Boolean> getAvailablePeriods() { return availablePeriods; }
    public void setAvailablePeriods(Map<String, Boolean> availablePeriods) { this.availablePeriods = availablePeriods; }

    public List<Schedule> getSchedules() { return schedules; }
    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    /**
    * Représente l’horaire d’un cours pour un trimestre donné.
    *
    * <p>Un horaire regroupe plusieurs sections
    * (cours magistraux, TP, laboratoires, etc.).</p>
    */
    public static class Schedule {

        @JsonProperty("fetch_date")
        private String fetchDate;

        private String name;
        private List<Section> sections;

        public Schedule() {}

        public String getFetchDate() { return fetchDate; }
        public void setFetchDate(String fetchDate) { this.fetchDate = fetchDate; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<Section> getSections() { return sections; }
        public void setSections(List<Section> sections) { this.sections = sections; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    /**
    * Représente une section d’activité d’un cours
    * (cours magistral, laboratoire, travaux pratiques, etc.).
    */
    public static class Section {

        private String name;
        private String capacity;

        @JsonProperty("number_inscription")
        private String numberInscription;

        private List<String> teachers;

        private List<Map<String, Object>> volets;

        public Section() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCapacity() { return capacity; }
        public void setCapacity(String capacity) { this.capacity = capacity; }

        public String getNumberInscription() { return numberInscription; }
        public void setNumberInscription(String numberInscription) { this.numberInscription = numberInscription; }

        public List<String> getTeachers() { return teachers; }
        public void setTeachers(List<String> teachers) { this.teachers = teachers; }

        public List<Map<String, Object>> getVolets() { return volets; }
        public void setVolets(List<Map<String, Object>> volets) { this.volets = volets; }
    }
}
