package com.diro.ift2255.controller;

import com.diro.ift2255.model.Review;
import com.diro.ift2255.service.ReviewService;
import com.diro.ift2255.util.ResponseUtil;
import io.javalin.http.Context;

public class ReviewController {

    private final ReviewService service;

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

    // POST /avis  (body: {courseId,difficulty,workload,comment?,author?})
    public void createReview(Context ctx) {
        Review review;
        try {
            review = ctx.bodyAsClass(Review.class);
        } catch (Exception e) {
            ctx.status(400).json(ResponseUtil.error("Body JSON invalide."));
            return;
        }

        boolean ok = service.addReview(review);
        if (!ok) {
            ctx.status(400).json(ResponseUtil.error("Avis invalide (courseId format, difficulty/workload 1..5)."));
            return;
        }

        ctx.status(201).json(ResponseUtil.ok(review));
    }
}
