package com.diro.ift2255.controller;

import io.javalin.http.Context;
import com.diro.ift2255.model.Course;
import com.diro.ift2255.service.CourseService;
import com.diro.ift2255.util.ResponseUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class CourseController {
    // Service qui contient la logique métier pour la manipulation des cours et la communication avec les services externes
    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    /**
     * Récupère la liste de tous les cours.
     * @param ctx Contexte Javalin représentant la requête et la réponse HTTP
     */
    public void getAllCourses(Context ctx) {
        Map<String, String> queryParams = extractQueryParams(ctx);

        List<Course> courses = service.getAllCourses(queryParams);
        ctx.json(courses);
    }

    /**
     * Récupère un cours spécifique par son ID.
     * @param ctx Contexte Javalin représentant la requête et la réponse HTTP
     */
    public void getCourseById(Context ctx) {
        String id = ctx.pathParam("id");

        if (!validateCourseId(id)) {
            ctx.status(400).json(ResponseUtil.formatError("Le paramètre id n'est pas valide."));
            return;
        }

        Optional<Course> course = service.getCourseById(id);
        if (course.isPresent()) {
            ctx.json(course.get());
        } else {
            ctx.status(404).json(ResponseUtil.formatError("Aucun cours ne correspond à l'ID: " + id));
        }
    }

    /**
     * Vérifie que l'ID du cours est bien formé
     * @param courseId L'ID du cours à valider
     * @return Valeur booléeene indiquant si l'ID est valide
     */
    private boolean validateCourseId(String courseId) {
        return courseId != null && courseId.trim().length() >= 6;
    }

        /**
     * Compare plusieurs cours à partir d'une liste d'IDs.
     * Exemple :
     *   GET /courses/comparer?ids=ARC1102,IFT2255,IFT2015
     */
    public void compareCourses(Context ctx) {
    String idsParam = ctx.queryParam("ids");

    if (idsParam == null || idsParam.isBlank()) {
        ctx.status(400).json(ResponseUtil.formatError(
                "Le paramètre 'ids' est requis (ex: ids=ARC1102,IFT2255)."));
        return;
    }

    String[] parts = idsParam.split(",");
    java.util.List<String> ids = new java.util.ArrayList<>();
    for (String p : parts) {
        if (p != null && !p.isBlank()) {
            ids.add(p.trim());
        }
    }

    if (ids.isEmpty()) {
        ctx.status(400).json(ResponseUtil.formatError(
                "Le paramètre 'ids' ne contient aucun identifiant valide."));
        return;
    }

    List<Course> courses = service.compareCourses(ids);
    ctx.json(courses);
}


    /**
     * Récupère tous les paramètres de requête depuis l'URL et les met dans une Map
     * @param ctx Contexte Javalin représentant la requête HTTP
     * @return Map contenant les paramètres de requête et leurs valeurs
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
