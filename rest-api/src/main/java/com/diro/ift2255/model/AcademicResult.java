// src/main/java/com/diro/ift2255/model/AcademicResult.java
package com.diro.ift2255.model;
/**
 * Représente les résultats académiques agrégés d’un cours.
 *
 * <p>Ces données proviennent du fichier CSV fourni et incluent :
 * le sigle du cours, son nom, la moyenne littérale obtenue, un score de réussite,
 * le nombre de participants et le nombre de trimestres observés.</p>
 */
public class AcademicResult {
    private String sigle;
    private String nom;
    private String moyenne;
    private double score;
    private int participants;
    private int trimestres;
    
    /**
    * Construit un {@code AcademicResult} vide.
    *
     * <p>Constructeur requis pour la désérialisation JSON ou CSV.</p>
    */
    public AcademicResult() {}

    /**
    * Construit un résultat académique agrégé pour un cours.
    *
    * @param sigle sigle du cours
    * @param nom nom du cours
    * @param moyenne moyenne littérale obtenue
    * @param score score de réussite (1 à 5)
    * @param participants nombre total de participants
    * @param trimestres nombre de trimestres observés
    */
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
