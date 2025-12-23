package com.diro.ift2255.service;

import com.diro.ift2255.model.Review;
import com.diro.ift2255.model.ReviewAggregate;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ReviewService.
 * Couvre les avis étudiants: consultation et soumission.
 */
public class ReviewServiceTest {

    private ReviewService reviewService;
    private static final String TEST_FILE = "target/test-reviews.json";

    @BeforeEach
    void setup() throws Exception {
        // Nettoyer le fichier de test avant chaque test
        Path testPath = Path.of(TEST_FILE);
        if (Files.exists(testPath)) {
            Files.delete(testPath);
        }
        reviewService = new ReviewService(TEST_FILE);
    }

    @AfterAll
    static void cleanup() throws Exception {
        // Nettoyer après tous les tests
        Path testPath = Path.of(TEST_FILE);
        if (Files.exists(testPath)) {
            Files.delete(testPath);
        }
    }

    // ========================================================================
    // CU : Soumettre un avis pour un cours
    // ========================================================================

    @Test
    @DisplayName("CU Avis - addReview ajoute un avis valide")
    void testAddReview_avisValide() {
        Review review = new Review();
        review.setCourseId("IFT2255");
        review.setDifficulty(4);
        review.setWorkload(3);
        review.setComment("Cours intéressant");

        boolean result = reviewService.addReview(review);

        assertTrue(result, "L'avis doit être ajouté");
        List<Review> reviews = reviewService.getReviewsForCourse("IFT2255");
        assertEquals(1, reviews.size());
        assertEquals("IFT2255", reviews.get(0).getCourseId());
    }

    @Test
    @DisplayName("CU Avis - addReview refuse un courseId invalide")
    void testAddReview_refuseCourseIdInvalide() {
        Review review = new Review();
        review.setCourseId("INVALID");
        review.setDifficulty(3);
        review.setWorkload(3);

        boolean result = reviewService.addReview(review);

        assertFalse(result, "L'avis avec courseId invalide doit être refusé");
    }

    @Test
    @DisplayName("CU Avis - addReview refuse difficulté hors limites")
    void testAddReview_refuseDifficulteInvalide() {
        Review reviewTooLow = new Review();
        reviewTooLow.setCourseId("IFT2255");
        reviewTooLow.setDifficulty(0);
        reviewTooLow.setWorkload(3);

        Review reviewTooHigh = new Review();
        reviewTooHigh.setCourseId("IFT2255");
        reviewTooHigh.setDifficulty(6);
        reviewTooHigh.setWorkload(3);

        assertFalse(reviewService.addReview(reviewTooLow), "Difficulté 0 doit être refusée");
        assertFalse(reviewService.addReview(reviewTooHigh), "Difficulté 6 doit être refusée");
    }

    @Test
    @DisplayName("CU Avis - addReview refuse workload hors limites")
    void testAddReview_refuseWorkloadInvalide() {
        Review reviewTooLow = new Review();
        reviewTooLow.setCourseId("IFT2255");
        reviewTooLow.setDifficulty(3);
        reviewTooLow.setWorkload(0);

        Review reviewTooHigh = new Review();
        reviewTooHigh.setCourseId("IFT2255");
        reviewTooHigh.setDifficulty(3);
        reviewTooHigh.setWorkload(6);

        assertFalse(reviewService.addReview(reviewTooLow), "Workload 0 doit être refusé");
        assertFalse(reviewService.addReview(reviewTooHigh), "Workload 6 doit être refusé");
    }

    @Test
    @DisplayName("CU Avis - addReview refuse un avis null")
    void testAddReview_refuseNull() {
        boolean result = reviewService.addReview(null);

        assertFalse(result, "Un avis null doit être refusé");
    }

    // ========================================================================
    // CU : Voir les avis étudiants pour un cours
    // ========================================================================

    @Test
    @DisplayName("CU Avis - getReviewsForCourse retourne les avis du cours")
    void testGetReviewsForCourse_retourneAvisDuCours() {
        // Ajouter plusieurs avis
        Review r1 = new Review();
        r1.setCourseId("IFT2255");
        r1.setDifficulty(4);
        r1.setWorkload(3);
        reviewService.addReview(r1);

        Review r2 = new Review();
        r2.setCourseId("IFT2255");
        r2.setDifficulty(3);
        r2.setWorkload(4);
        reviewService.addReview(r2);

        Review r3 = new Review();
        r3.setCourseId("IFT1015");  // Autre cours
        r3.setDifficulty(2);
        r3.setWorkload(2);
        reviewService.addReview(r3);

        List<Review> reviews = reviewService.getReviewsForCourse("IFT2255");

        assertEquals(2, reviews.size(), "Seuls les avis du cours IFT2255 doivent être retournés");
    }

    @Test
    @DisplayName("CU Avis - getReviewsForCourse retourne liste vide si aucun avis")
    void testGetReviewsForCourse_retourneVideSiAucunAvis() {
        List<Review> reviews = reviewService.getReviewsForCourse("IFT9999");

        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
    }

    @Test
    @DisplayName("CU Avis - getReviewsForCourse gère courseId null")
    void testGetReviewsForCourse_gereNull() {
        List<Review> reviews = reviewService.getReviewsForCourse(null);

        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
    }

    // ========================================================================
    // CU : Avis agrégés
    // ========================================================================

    @Test
    @DisplayName("CU Avis - getAggregateForCourse calcule les moyennes correctement")
    void testGetAggregateForCourse_calculeMoyennes() {
        // Ajouter des avis
        Review r1 = new Review();
        r1.setCourseId("IFT2255");
        r1.setDifficulty(4);
        r1.setWorkload(2);
        reviewService.addReview(r1);

        Review r2 = new Review();
        r2.setCourseId("IFT2255");
        r2.setDifficulty(2);
        r2.setWorkload(4);
        reviewService.addReview(r2);

        ReviewAggregate aggregate = reviewService.getAggregateForCourse("IFT2255");

        assertEquals("IFT2255", aggregate.getCourseId());
        assertEquals(2, aggregate.getCount());
        assertEquals(3.0, aggregate.getAvgDifficulty(), 0.01);
        assertEquals(3.0, aggregate.getAvgWorkload(), 0.01);
    }

    @Test
    @DisplayName("CU Avis - getAggregateForCourse retourne agrégat vide si aucun avis")
    void testGetAggregateForCourse_agregateVideSiAucunAvis() {
        ReviewAggregate aggregate = reviewService.getAggregateForCourse("IFT9999");

        assertNotNull(aggregate);
        assertEquals(0, aggregate.getCount());
        assertEquals(0.0, aggregate.getAvgDifficulty(), 0.01);
        assertEquals(0.0, aggregate.getAvgWorkload(), 0.01);
    }
}
