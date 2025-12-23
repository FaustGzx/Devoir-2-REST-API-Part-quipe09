package com.diro.ift2255.controller;

import com.diro.ift2255.model.Review;
import com.diro.ift2255.service.ReviewService;
import com.diro.ift2255.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;

public class ReviewController {

    private final ReviewService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    // GET /avis/{courseId}
    public void getReviews(Context ctx) {
        String courseId = ctx.pathParam("courseId");
        if (courseId == null || !courseId.trim().toUpperCase().matches("^[A-Z]{3}\\d{4}$")) {
            ctx.status(400).json(ResponseUtil.error("courseId invalide (ex: IFT2255)."));
            return;
        }
        ctx.json(ResponseUtil.ok(service.getAggregateForCourse(courseId)));
    }

    // POST /avis
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
