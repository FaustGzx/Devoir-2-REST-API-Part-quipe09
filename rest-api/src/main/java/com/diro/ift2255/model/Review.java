package com.diro.ift2255.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Représente un avis étudiant associé à un cours.
 *
 * <p>Un {@code Review} contient une évaluation de la difficulté,
 * de la charge de travail ainsi qu’un commentaire optionnel
 * fourni par un étudiant.</p>
 */
public class Review {

    private String courseId;     // IFT2255
    private int difficulty;      // 1..5
    private int workload;        // 1..5
    private String comment;      // optionnel
    private String author;       // optionnel (discord user)
    private long timestamp;      // epoch ms

    public Review() {}

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public int getWorkload() { return workload; }
    public void setWorkload(int workload) { this.workload = workload; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

