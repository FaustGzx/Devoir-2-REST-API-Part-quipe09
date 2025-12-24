package com.diro.ift2255.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Program {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    // certains endpoints peuvent renvoyer "courses" ou "program_courses"
    @JsonProperty("courses")
    private List<ProgramCourse> courses;

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
