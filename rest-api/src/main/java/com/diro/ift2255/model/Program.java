package com.diro.ift2255.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Représente un programme académique de l’Université de Montréal.
 *
 * <p>Un {@code Program} contient les informations générales d’un programme
 * (identifiant, nom, description) ainsi que la liste des cours qui le composent.</p>
 */
public class Program {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    // certains endpoints peuvent renvoyer "courses" ou "program_courses"
    @JsonProperty("courses")
    private List<ProgramCourse> courses;
    /**
    * Construit un {@code Program} vide.
    *
    * <p>Utilisé pour la sérialisation/désérialisation JSON.</p>
    */
    public Program() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ProgramCourse> getCourses() { return courses; }
    public void setCourses(List<ProgramCourse> courses) { this.courses = courses; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProgramCourse {

        @JsonProperty("id")
        private String id;   // ex: IFT2255

        @JsonProperty("name")
        private String name; // ex: Génie logiciel

        public ProgramCourse() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
