package com.diro.ift2255.model;
/**
 * Représente un élément utilisé pour comparer des cours entre eux.
 *
 * <p>Un {@code CompareItem} agrège plusieurs indicateurs provenant
 * des avis étudiants, des résultats académiques et du catalogue
 * afin de faciliter la comparaison (charge de travail, difficulté, réussite, etc.).</p>
 */
public class CompareItem {
    private String id;
    private String name;

    // Avis
    private int reviewCount;
    private double avgDifficulty;
    private double avgWorkload;

    // CSV
    private Double csvScore;      // null si absent
    private Integer participants; // null si absent
    private String moyenne;       // null si absent
    /**
    * Construit un {@code CompareItem} vide.
    *
    * <p>Utilisé principalement pour la désérialisation JSON.</p>
    */
    public CompareItem() {}
    /**
    * Construit un élément de comparaison de cours.
    *
    * @param id identifiant du cours
    * @param name nom du cours
    * @param avgDifficulty difficulté moyenne estimée
    * @param avgWorkload charge de travail moyenne estimée
    * @param csvScore score de réussite académique
    * @param participants nombre de participants
    * @param reviewCount nombre d’avis étudiants
    * @param moyenne moyenne littérale observée
    */
    public CompareItem(String id, String name,
                       int reviewCount, double avgDifficulty, double avgWorkload,
                       Double csvScore, Integer participants, String moyenne) {
        this.id = id;
        this.name = name;
        this.reviewCount = reviewCount;
        this.avgDifficulty = avgDifficulty;
        this.avgWorkload = avgWorkload;
        this.csvScore = csvScore;
        this.participants = participants;
        this.moyenne = moyenne;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getReviewCount() { return reviewCount; }
    public double getAvgDifficulty() { return avgDifficulty; }
    public double getAvgWorkload() { return avgWorkload; }
    public Double getCsvScore() { return csvScore; }
    public Integer getParticipants() { return participants; }
    public String getMoyenne() { return moyenne; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setAvgDifficulty(double avgDifficulty) { this.avgDifficulty = avgDifficulty; }
    public void setAvgWorkload(double avgWorkload) { this.avgWorkload = avgWorkload; }
    public void setCsvScore(Double csvScore) { this.csvScore = csvScore; }
    public void setParticipants(Integer participants) { this.participants = participants; }
    public void setMoyenne(String moyenne) { this.moyenne = moyenne; }
}
