package com.diro.ift2255.controller;

import com.diro.ift2255.model.CourseSet;
import com.diro.ift2255.service.CourseSetService;
import com.diro.ift2255.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;

public class CourseSetController {

    private final CourseSetService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public CourseSetController(CourseSetService service) {
        this.service = service;
    }

    // POST /sets  body: { "semester":"H25", "courseIds":["IFT2255","IFT1025"] }
    public void createSet(Context ctx) {
        try {
            String raw = ctx.body();
            if (raw == null || raw.isBlank()) {
                ctx.status(400).json(ResponseUtil.error("Body vide. Envoyez un JSON (Content-Type: application/json)."));
                return;
            }

            // FIX: enlever BOM + espaces
            raw = raw.replace("\uFEFF", "").trim();

            CourseSet body = mapper.readValue(raw, CourseSet.class);

            if (body.getSemester() == null || body.getCourseIds() == null) {
                ctx.status(400).json(ResponseUtil.error("semester et courseIds sont requis."));
                return;
            }

            var opt = service.createSet(body.getSemester(), body.getCourseIds());
            if (opt.isEmpty()) {
                ctx.status(400).json(ResponseUtil.error(
                        "Ensemble invalide (semester H25/A24/E24, 1..6 cours, ids valides)."
                ));
                return;
            }

            ctx.status(201).json(ResponseUtil.ok(opt.get()));

        } catch (Exception e) {
            ctx.status(400).json(ResponseUtil.error("Body JSON invalide: " + e.getMessage()));
        }
    }

    // GET /sets/{id}
    public void getSet(Context ctx) {
        String id = ctx.pathParam("id");
        var opt = service.getSet(id);
        if (opt.isEmpty()) {
            ctx.status(404).json(ResponseUtil.error("Ensemble introuvable: " + id));
            return;
        }
        ctx.json(ResponseUtil.ok(opt.get()));
    }

    // GET /sets/{id}/schedule
    public void getSetSchedule(Context ctx) {
        String id = ctx.pathParam("id");
        var opt = service.getSet(id);
        if (opt.isEmpty()) {
            ctx.status(404).json(ResponseUtil.error("Ensemble introuvable: " + id));
            return;
        }
        ctx.json(ResponseUtil.ok(service.getSetSchedule(id)));
    }
}
