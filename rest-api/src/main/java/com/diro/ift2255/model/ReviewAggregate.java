package com.diro.ift2255.model;

import java.util.List;

public class ReviewAggregate {
    private String courseId;
    private int count;
    private double avgDifficulty;
    private double avgWorkload;
    private List<Review> reviews;

    public ReviewAggregate(String courseId, int count, double avgDifficulty, double avgWorkload, List<Review> reviews) {
        this.courseId = courseId;
        this.count = count;
        this.avgDifficulty = avgDifficulty;
        this.avgWorkload = avgWorkload;
        this.reviews = reviews;
    }

    public String getCourseId() { return courseId; }
    public int getCount() { return count; }
    public double getAvgDifficulty() { return avgDifficulty; }
    public double getAvgWorkload() { return avgWorkload; }
    public List<Review> getReviews() { return reviews; }
}
