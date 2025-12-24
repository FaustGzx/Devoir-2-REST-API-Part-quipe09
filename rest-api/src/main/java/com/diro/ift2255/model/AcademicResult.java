// src/main/java/com/diro/ift2255/model/AcademicResult.java
package com.diro.ift2255.model;

public class AcademicResult {
    private String sigle;
    private String nom;
    private String moyenne;
    private double score;
    private int participants;
    private int trimestres;

    public AcademicResult() {}

    public AcademicResult(String sigle, String nom, String moyenne, double score, int participants, int trimestres) {
        this.sigle = sigle;
        this.nom = nom;
        this.moyenne = moyenne;
        this.score = score;
        this.participants = participants;
        this.trimestres = trimestres;
    }

    public String getSigle() { return sigle; }
    public String getNom() { return nom; }
    public String getMoyenne() { return moyenne; }
    public double getScore() { return score; }
    public int getParticipants() { return participants; }
    public int getTrimestres() { return trimestres; }
}
