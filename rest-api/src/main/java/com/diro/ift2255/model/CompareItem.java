package com.diro.ift2255.model;

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

    public CompareItem() {}

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
