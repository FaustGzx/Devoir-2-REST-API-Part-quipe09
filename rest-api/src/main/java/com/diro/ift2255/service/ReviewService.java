package com.diro.ift2255.service;

import com.diro.ift2255.model.Review;
import com.diro.ift2255.model.ReviewAggregate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Service responsable de la gestion des avis étudiants.
 *
 * <p>Ce service permet de stocker, récupérer et agréger
 * les avis soumis par les étudiants pour les cours,
 * incluant la difficulté perçue et la charge de travail.</p>
 */

public class ReviewService {

    private final Path storagePath;
    private final ObjectMapper mapper = new ObjectMapper();

    // Simple en mémoire + flush sur disque
    private final List<Review> all = new ArrayList<>();

    public ReviewService(String filePath) {
        this.storagePath = Path.of(filePath);
        load();
    }

    private void load() {
        try {
            if (!Files.exists(storagePath)) return;
            byte[] bytes = Files.readAllBytes(storagePath);
            if (bytes.length == 0) return;

            List<Review> loaded = mapper.readValue(bytes, new TypeReference<List<Review>>() {});
            if (loaded != null) {
                all.clear();
                all.addAll(loaded);
            }
        } catch (Exception ignored) {
        }
    }

    private void save() {
        try {
            Files.createDirectories(storagePath.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(storagePath.toFile(), all);
        } catch (Exception ignored) {
        }
    }

    public List<Review> getReviewsForCourse(String courseId) {
        if (courseId == null) return List.of();
        String id = courseId.trim().toUpperCase();
        return all.stream()
                .filter(r -> r.getCourseId() != null && r.getCourseId().trim().toUpperCase().equals(id))
                .collect(Collectors.toList());
    }

    public ReviewAggregate getAggregateForCourse(String courseId) {
        List<Review> reviews = getReviewsForCourse(courseId);
        int n = reviews.size();

        if (n == 0) {
            return new ReviewAggregate(courseId.toUpperCase(), 0, 0.0, 0.0, List.of());
        }

        double avgDiff = reviews.stream().mapToInt(Review::getDifficulty).average().orElse(0.0);
        double avgWork = reviews.stream().mapToInt(Review::getWorkload).average().orElse(0.0);

        return new ReviewAggregate(courseId.toUpperCase(), n, avgDiff, avgWork, reviews);
    }

    public boolean addReview(Review review) {
        if (review == null) return false;

        String id = (review.getCourseId() == null) ? "" : review.getCourseId().trim().toUpperCase();
        if (!id.matches("^[A-Z]{3}\\d{4}$")) return false;

        if (review.getDifficulty() < 1 || review.getDifficulty() > 5) return false;
        if (review.getWorkload() < 1 || review.getWorkload() > 5) return false;

        review.setCourseId(id);
        if (review.getTimestamp() <= 0) review.setTimestamp(System.currentTimeMillis());

        all.add(review);
        save();
        return true;
    }
}


