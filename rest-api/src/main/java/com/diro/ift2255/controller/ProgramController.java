package com.diro.ift2255.controller;

import com.diro.ift2255.service.ProgramService;
import com.diro.ift2255.util.ResponseUtil;
import io.javalin.http.Context;
/**
 * Contrôleur REST (Javalin) responsable des opérations liées aux programmes académiques.
 *
 * <p>Permet :</p>
 * <ul>
 *   <li>de consulter les informations d’un programme</li>
 *   <li>de lister les cours offerts dans un programme pour un trimestre donné</li>
 * </ul>
 *
 * Les réponses sont formatées de manière standard via {@link com.diro.ift2255.util.ResponseUtil}.
 */

public class ProgramController {
    /** Service applicatif fournissant l’accès aux programmes et à leurs cours associés. */
    private final ProgramService service;
    /**
    * Construit un {@code ProgramController}.
    *
    * @param service service de gestion des programmes académiques
    */
    public ProgramController(ProgramService service) {
        this.service = service;
    }

    /**
    * Retourne les informations d’un programme à partir de son identifiant.
    *
    * <p>Endpoint : {@code GET /programs/{programId}}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code programId} + réponse JSON)
    */
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

    /**
    * Retourne la liste des cours offerts dans un programme pour un trimestre donné.
    *
    * <p>Endpoint : {@code GET /programs/{programId}/courses?semester=H25}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code programId},
    *            paramètre de requête {@code semester} + réponse JSON)
    */
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
