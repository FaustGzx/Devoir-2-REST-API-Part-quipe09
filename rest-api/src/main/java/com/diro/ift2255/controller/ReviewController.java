package com.diro.ift2255.controller;

import com.diro.ift2255.model.Review;
import com.diro.ift2255.service.ReviewService;
import com.diro.ift2255.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
/**
 * Contrôleur REST (Javalin) responsable des avis étudiants pour un cours.
 *
 * <p>Permet :</p>
 * <ul>
 *   <li>de consulter la liste des avis pour un cours</li>
 *   <li>de consulter les statistiques agrégées (moyenne difficulté/charge)</li>
 *   <li>de soumettre un nouvel avis (ex: via le bot Discord)</li>
 * </ul>
 *
 * Les réponses JSON sont standardisées via {@link com.diro.ift2255.util.ResponseUtil}.
 */
public class ReviewController {
    /** Service applicatif responsable de la gestion, validation et agrégation des avis. */
    private final ReviewService service;
    /** Mapper JSON utilisé pour lire le corps des requêtes (POST) et sérialiser/désérialiser les avis. */
    private final ObjectMapper mapper = new ObjectMapper();
    /**
    * Construit un {@code ReviewController}.
    *
    * @param service service de gestion des avis
    */
    public ReviewController(ReviewService service) {
        this.service = service;
    }

    /**
    * Retourne la liste des avis pour un cours donné.
    *
    * <p>Endpoint : {@code GET /reviews/{courseId}}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code courseId} + réponse JSON)
    */
    public void getReviews(Context ctx) {
        String courseId = ctx.pathParam("courseId");
        if (courseId == null || !courseId.trim().toUpperCase().matches("^[A-Z]{3}\\d{4}$")) {
            ctx.status(400).json(ResponseUtil.error("courseId invalide (ex: IFT2255)."));
            return;
        }
        ctx.json(ResponseUtil.ok(service.getReviewsForCourse(courseId.toUpperCase())));
    }

    /**
    * Retourne les statistiques agrégées des avis pour un cours (ex: moyenne difficulté/charge).
    *
    * <p>Endpoint : {@code GET /reviews/{courseId}/aggregate}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code courseId} + réponse JSON)
    */
    public void getAggregate(Context ctx) {
        String courseId = ctx.pathParam("courseId");
        if (courseId == null || !courseId.trim().toUpperCase().matches("^[A-Z]{3}\\d{4}$")) {
            ctx.status(400).json(ResponseUtil.error("courseId invalide (ex: IFT2255)."));
            return;
        }
        ctx.json(ResponseUtil.ok(service.getAggregateForCourse(courseId.toUpperCase())));
    }

    /**
    * Crée un nouvel avis étudiant pour un cours.
    *
    * <p>Endpoint : {@code POST /reviews}</p>
    * <p>Corps JSON attendu (exemple) :</p>
    * <pre>{@code
    * { "courseId":"IFT2255", "difficulty":4, "workload":3, "comment":"..." }
    * }</pre>
    *
    * <p>En cas de données invalides, le contrôleur retourne une erreur conviviale (400).</p>
    *
    * @param ctx contexte Javalin (corps JSON + réponse JSON)
    */
    public void createReview(Context ctx) {
        try {
            String raw = ctx.body();
            if (raw == null || raw.isBlank()) {
                ctx.status(400).json(ResponseUtil.error("Body vide. Envoyez un JSON (Content-Type: application/json)."));
                return;
            }

            // FIX: enlever BOM + espaces (PowerShell ajoute souvent un BOM UTF-8)
            raw = raw.replace("\uFEFF", "").trim();

            Review review = mapper.readValue(raw, Review.class);

            boolean ok = service.addReview(review);
            if (!ok) {
                ctx.status(400).json(ResponseUtil.error("Avis invalide (courseId format, difficulty/workload 1..5)."));
                return;
            }

            ctx.status(201).json(ResponseUtil.ok(review));

        } catch (Exception e) {
            ctx.status(400).json(ResponseUtil.error("Body JSON invalide: " + e.getMessage()));
        }
    }
}
