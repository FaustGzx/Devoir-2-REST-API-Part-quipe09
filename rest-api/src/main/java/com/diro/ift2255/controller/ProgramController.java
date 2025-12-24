package com.diro.ift2255.controller;

import com.diro.ift2255.service.ProgramService;
import com.diro.ift2255.util.ResponseUtil;
import io.javalin.http.Context;

public class ProgramController {

    private final ProgramService service;

    public ProgramController(ProgramService service) {
        this.service = service;
    }

    // GET /programs/{id}?response_level=min|reg|full
    public void getProgram(Context ctx) {
        String id = ctx.pathParam("id");
        String level = ctx.queryParam("response_level");

        var opt = service.getProgram(id, level);
        if (opt.isEmpty()) {
            ctx.status(404).json(ResponseUtil.error("Programme introuvable: " + id));
            return;
        }

        ctx.json(ResponseUtil.ok(opt.get()));
    }

    // GET /programs/{id}/courses?semester=H25&limit=200
    public void getProgramCoursesOfferedInSemester(Context ctx) {
        String id = ctx.pathParam("id");
        String semester = ctx.queryParam("semester");
        String limitParam = ctx.queryParam("limit");

        if (semester == null || semester.isBlank()) {
            ctx.status(400).json(ResponseUtil.error("Le paramètre 'semester' est requis (ex: H25, A24, E24)."));
            return;
        }

        int limit = 200;
        if (limitParam != null && !limitParam.isBlank()) {
            try {
                limit = Integer.parseInt(limitParam.trim());
            } catch (NumberFormatException e) {
                ctx.status(400).json(ResponseUtil.error("Le paramètre 'limit' doit être un entier."));
                return;
            }
        }

        var courses = service.getProgramCoursesOfferedInSemester(id, semester, limit);
        ctx.json(ResponseUtil.ok(courses));
    }
}
