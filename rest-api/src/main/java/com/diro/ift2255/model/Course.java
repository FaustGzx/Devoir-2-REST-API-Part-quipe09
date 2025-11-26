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

    // Champs supplémentaires vus dans le screenshot Planifium
    private String prerequis;

    @JsonProperty("prerequisite_courses")
    private List<String> prerequisiteCourses;

    private String semester;

    @JsonProperty("semester_int")
    private Integer semesterInt;

    private String sigle;

    // Liste des horaires / offres du cours
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

    // (bug corrigé ici : ce n’est plus "email")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrerequis() {
        return prerequis;
    }

    public void setPrerequis(String prerequis) {
        this.prerequis = prerequis;
    }

    public List<String> getPrerequisiteCourses() {
        return prerequisiteCourses;
    }

    public void setPrerequisiteCourses(List<String> prerequisiteCourses) {
        this.prerequisiteCourses = prerequisiteCourses;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Integer getSemesterInt() {
        return semesterInt;
    }

    public void setSemesterInt(Integer semesterInt) {
        this.semesterInt = semesterInt;
    }

    public String getSigle() {
        return sigle;
    }

    public void setSigle(String sigle) {
        this.sigle = sigle;
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

        // Dans le JSON c’est "capacity": "130" → on garde String pour ne pas casser
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
