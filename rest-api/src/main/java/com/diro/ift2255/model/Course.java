package com.diro.ift2255.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {

    // Champs de base
    private String id;
    private String name;
    private String description;

    // Champs supplémentaires pertinents de l'API Planifium
    @JsonProperty("prerequisite_courses")
    private List<String> prerequisiteCourses;

    // Nombre de crédits du cours
    private Double credits;

    // Texte lisible des prérequis (ex: "Préalable : IFT1025")
    @JsonProperty("requirement_text")
    private String requirementText;

    // Trimestres où le cours est offert (autumn, winter, summer)
    @JsonProperty("available_terms")
    private Map<String, Boolean> availableTerms;

    // Périodes où le cours est offert (daytime, evening)
    @JsonProperty("available_periods")
    private Map<String, Boolean> availablePeriods;

    // Liste des horaires / offres du cours (sections, profs, etc.)
    private List<Schedule> schedules;

    // ----- Constructeurs -----

    public Course() {}

    public Course(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // ----- Getters / Setters -----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    // (bug corrigé : ce n'est plus "email")
    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPrerequisiteCourses() {
        return prerequisiteCourses;
    }

    public void setPrerequisiteCourses(List<String> prerequisiteCourses) {
        this.prerequisiteCourses = prerequisiteCourses;
    }

    public Double getCredits() {
        return credits;
    }

    public void setCredits(Double credits) {
        this.credits = credits;
    }

    public String getRequirementText() {
        return requirementText;
    }

    public void setRequirementText(String requirementText) {
        this.requirementText = requirementText;
    }

    public Map<String, Boolean> getAvailableTerms() {
        return availableTerms;
    }

    public void setAvailableTerms(Map<String, Boolean> availableTerms) {
        this.availableTerms = availableTerms;
    }

    public Map<String, Boolean> getAvailablePeriods() {
        return availablePeriods;
    }

    public void setAvailablePeriods(Map<String, Boolean> availablePeriods) {
        this.availablePeriods = availablePeriods;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    // ========================================================================
    // Classes imbriquées pour mapper schedules / sections
    // ========================================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Schedule {

        @JsonProperty("fetch_date")
        private String fetchDate;

        private String name;

        private List<Section> sections;

        public Schedule() {}

        public String getFetchDate() {
            return fetchDate;
        }

        public void setFetchDate(String fetchDate) {
            this.fetchDate = fetchDate;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Section> getSections() {
            return sections;
        }

        public void setSections(List<Section> sections) {
            this.sections = sections;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {

        private String name;

        // Dans le JSON c’est "capacity": "130" → on garde String pour être souple
        private String capacity;

        @JsonProperty("number_inscription")
        private String numberInscription;

        // Liste des profs ['Feeley, Marc', ...]
        private List<String> teachers;

        // On ne connaît pas exactement la structure des "volets",
        // donc on reste générique : une liste de maps clé/valeur
        private List<Map<String, Object>> volets;

        public Section() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCapacity() {
            return capacity;
        }

        public void setCapacity(String capacity) {
            this.capacity = capacity;
        }

        public String getNumberInscription() {
            return numberInscription;
        }

        public void setNumberInscription(String numberInscription) {
            this.numberInscription = numberInscription;
        }

        public List<String> getTeachers() {
            return teachers;
        }

        public void setTeachers(List<String> teachers) {
            this.teachers = teachers;
        }

        public List<Map<String, Object>> getVolets() {
            return volets;
        }

        public void setVolets(List<Map<String, Object>> volets) {
            this.volets = volets;
        }
    }
}
