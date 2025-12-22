package com.diro.ift2255.controller;

import io.javalin.http.Context;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.EligibilityResult;
import com.diro.ift2255.service.AcademicResultService;
import com.diro.ift2255.service.CourseService;
import com.diro.ift2255.util.ResponseUtil;

import java.util.*;

public class CourseController {

    private final CourseService service;
    private final AcademicResultService resultsService;

    public CourseController(CourseService service) {
        this.service = service;
        // Le fichier doit être dans src/main/resources/
        this.resultsService = new AcademicResultService("historique_cours_prog_117510.csv");
    }

    // Validation type: IFT2255 (3 lettres + 4 chiffres)
    private boolean validateCourseId(String courseId) {
        return courseId != null && courseId.trim().matches("(?i)^[A-Z]{3}\\d{4}$");
    }

    /**
     * CU1 - Recherche / liste de cours avec filtres Planifium
     * Exemple:
     *  GET /courses?name=logiciel
     *  GET /courses?description=java
     *  GET /courses?courses_sigle=ift1015,ift1025
     */
    public void getAllCourses(Context ctx) {
        Map<String, String> queryParams = extractQueryParams(ctx);
        List<Course> courses = service.getAllCourses(queryParams);
        ctx.json(ResponseUtil.ok(courses));
    }

    /**
     * CU2 - Détails d'un cours (peut inclure horaires via query params Planifium)
     * Exemple:
     *  GET /courses/IFT2255
     *  GET /courses/IFT2255?include_schedule=true&schedule_semester=a25
     */
    public void getCourseById(Context ctx) {
        String id = ctx.pathParam("id");

        if (!validateCourseId(id)) {
            ctx.status(400).json(ResponseUtil.error("Le paramètre id n'est pas valide (ex: IFT2255)."));
            return;
        }

        Map<String, String> queryParams = extractQueryParams(ctx);
        Optional<Course> course = service.getCourseById(id, queryParams);

        if (course.isPresent()) {
            ctx.json(ResponseUtil.ok(course.get()));
        } else {
            ctx.status(404).json(ResponseUtil.error("Aucun cours ne correspond à l'ID: " + id));
        }
    }

    /**
     * CU3 - Comparer des cours
     * Exemple:
     *  GET /courses/comparer?ids=ARC1102,IFT2255,IFT2015
     */
    public void compareCourses(Context ctx) {
        String idsParam = ctx.queryParam("ids");

        if (idsParam == null || idsParam.isBlank()) {
            ctx.status(400).json(ResponseUtil.error("Le paramètre 'ids' est requis (ex: ids=ARC1102,IFT2255)."));
            return;
        }

        List<String> ids = Arrays.stream(idsParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (ids.isEmpty()) {
            ctx.status(400).json(ResponseUtil.error("Le paramètre 'ids' ne contient aucun identifiant valide."));
            return;
        }

        // On récupère tous les query params et on enlève ids pour ne pas le renvoyer à Planifium
        Map<String, String> queryParams = extractQueryParams(ctx);
        queryParams.remove("ids");

        List<Course> courses = service.compareCourses(ids, queryParams);
        ctx.json(ResponseUtil.ok(courses));
    }

    /**
     * Vérifier l’éligibilité d’un étudiant à un cours selon les prérequis + cours complétés.
     * Exemple:
     *  GET /courses/IFT2255/eligibility?completed=IFT1015,IFT1025&cycle=1
     */
    public void getEligibility(Context ctx) {
        String id = ctx.pathParam("id");
        if (!validateCourseId(id)) {
            ctx.status(400).json(ResponseUtil.error("Le paramètre id n'est pas valide (ex: IFT2255)."));
            return;
        }

        String completedParam = ctx.queryParam("completed");
        List<String> completed = (completedParam == null || completedParam.isBlank())
                ? List.of()
                : Arrays.stream(completedParam.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

        Integer cycle = null;
        String cycleParam = ctx.queryParam("cycle");
        if (cycleParam != null && !cycleParam.isBlank()) {
            try {
                cycle = Integer.parseInt(cycleParam.trim());
            } catch (NumberFormatException e) {
                ctx.status(400).json(ResponseUtil.error("Le paramètre cycle doit être un entier (1, 2 ou 3)."));
                return;
            }
        }

        EligibilityResult result = service.checkEligibility(id, completed, cycle);
        ctx.json(ResponseUtil.ok(result));
    }

    /**
     * Résultats académiques agrégés (CSV)
     * Exemple:
     *  GET /courses/IFT2255/results
     */
    public void getAcademicResults(Context ctx) {
        String id = ctx.pathParam("id");
        if (!validateCourseId(id)) {
            ctx.status(400).json(ResponseUtil.error("Le paramètre id n'est pas valide (ex: IFT2255)."));
            return;
        }

        var opt = resultsService.getBySigle(id);
        if (opt.isEmpty()) {
            ctx.status(404).json(ResponseUtil.error("Aucun résultat académique trouvé pour: " + id));
            return;
        }

        ctx.json(ResponseUtil.ok(opt.get()));
    }

    /**
     * Utilitaire: récupère tous les paramètres de requête dans une Map
     */
    private Map<String, String> extractQueryParams(Context ctx) {
        Map<String, String> queryParams = new HashMap<>();

        ctx.queryParamMap().forEach((key, values) -> {
            if (!values.isEmpty()) {
                queryParams.put(key, values.get(0));
            }
        });

        return queryParams;
    }
}
