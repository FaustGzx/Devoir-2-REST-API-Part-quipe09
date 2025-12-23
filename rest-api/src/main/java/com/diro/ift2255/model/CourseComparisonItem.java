package com.diro.ift2255.model;

public class CourseComparisonItem {
    private String courseId;
    private String name;

    private double avgWorkload;
    private double avgDifficulty;  // ici on utilise avgDifficulty des avis
    private Double successScore;   // score CSV (1..5), peut être null si absent

    public CourseComparisonItem(String courseId, String name, double avgWorkload, double avgDifficulty, Double successScore) {
        this.courseId = courseId;
        this.name = name;
        this.avgWorkload = avgWorkload;
        this.avgDifficulty = avgDifficulty;
        this.successScore = successScore;
    }

    public String getCourseId() { return courseId; }
    public String getName() { return name; }
    public double getAvgWorkload() { return avgWorkload; }
    public double getAvgDifficulty() { return avgDifficulty; }
    public Double getSuccessScore() { return successScore; }
}
