package com.diro.ift2255.controller;

import com.diro.ift2255.model.CourseSet;
import com.diro.ift2255.service.CourseSetService;
import com.diro.ift2255.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;
/**
 * Contrôleur REST (Javalin) pour la gestion des ensembles de cours (CourseSet).
 *
 * <p>Permet :</p>
 * <ul>
 *   <li>de créer un ensemble (max 6 cours) pour un trimestre donné</li>
 *   <li>de récupérer un ensemble existant</li>
 *   <li>de consulter l'horaire résultant d'un ensemble</li>
 *   <li>de détecter les conflits d'horaire (bonus) pour un ensemble</li>
 * </ul>
 *
 * Les réponses JSON sont standardisées via {@link com.diro.ift2255.util.ResponseUtil}.
 */
public class CourseSetController {

    /** Service applicatif responsable de la création, consultation et analyse des ensembles de cours. */
    private final CourseSetService service;

    /** Mapper JSON utilisé pour lire le corps des requêtes (POST) et valider la structure du JSON. */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
    * Construit un {@code CourseSetController}.
    *
    * @param service service de gestion des ensembles de cours
    */
    public CourseSetController(CourseSetService service) {
        this.service = service;
    }

    /**
    * Crée un ensemble de cours (maximum 6 cours) pour un trimestre donné.
    *
    * <p>Endpoint : {@code POST /sets}</p>
    * <p>Corps JSON attendu (exemple) :</p>
    * <pre>{@code
    * { "semester":"H25", "courseIds":["IFT2255","IFT1025"] }
    * }</pre>
    *
    * <p>Validations principales :</p>
    * <ul>
    *   <li>{@code semester} requis (format H25/A24/E24)</li>
    *   <li>{@code courseIds} requis, non vide, sans doublons</li>
    *   <li>maximum 6 cours</li>
    *   <li>format de sigle : {@code ABC1234}</li>
    * </ul>
    *
    * @param ctx contexte Javalin (corps JSON de la requête + réponse JSON)
    */
    public void createSet(Context ctx) {
        try {
            String raw = ctx.body();
            if (raw == null || raw.isBlank()) {
                ctx.status(400).json(ResponseUtil.error("Body vide. Envoyez un JSON avec Content-Type: application/json."));
                return;
            }

            // FIX: enlever BOM + espaces
            raw = raw.replace("\uFEFF", "").trim();

            CourseSet body = mapper.readValue(raw, CourseSet.class);

            // Validation du trimestre
            if (body.getSemester() == null || body.getSemester().isBlank()) {
                ctx.status(400).json(ResponseUtil.error("Le champ 'semester' est requis (ex: H25, A24, E24)."));
                return;
            }

            String semNormalized = body.getSemester().trim().toUpperCase();
            if (!semNormalized.matches("^[HAE]\\d{2}$")) {
                ctx.status(400).json(ResponseUtil.error(
                        "Format de trimestre invalide: '" + body.getSemester() + "'. Utilisez H25, A24, E24, etc."));
                return;
            }

            // Validation des courseIds
            if (body.getCourseIds() == null || body.getCourseIds().isEmpty()) {
                ctx.status(400).json(ResponseUtil.error("Le champ 'courseIds' est requis et ne peut pas être vide."));
                return;
            }

            List<String> cleanedIds = body.getCourseIds().stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toUpperCase())
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .toList();

            if (cleanedIds.isEmpty()) {
                ctx.status(400).json(ResponseUtil.error("Le champ 'courseIds' ne contient aucun identifiant valide."));
                return;
            }

            if (cleanedIds.size() > 6) {
                ctx.status(400).json(ResponseUtil.error(
                        "Un ensemble peut contenir au maximum 6 cours. Vous avez fourni " + cleanedIds.size() + " cours."));
                return;
            }

            // Validation du format de chaque sigle
            for (String id : cleanedIds) {
                if (!id.matches("^[A-Z]{3}\\d{4}$")) {
                    ctx.status(400).json(ResponseUtil.error(
                            "Format de sigle invalide: '" + id + "'. Utilisez le format ABC1234 (ex: IFT2255)."));
                    return;
                }
            }

            // FIX: utiliser les valeurs nettoyées (cleanedIds, semNormalized)
            var opt = service.createSet(semNormalized, cleanedIds);
            if (opt.isEmpty()) {
                ctx.status(400).json(ResponseUtil.error(
                        "Impossible de créer l'ensemble. Vérifiez les paramètres."));
                return;
            }

            ctx.status(201).json(ResponseUtil.ok(opt.get()));

        } catch (Exception e) {
            ctx.status(400).json(ResponseUtil.error("Body JSON invalide: " + e.getMessage()));
        }
    }

    
    /**
    * Récupère un ensemble de cours par son identifiant.
    *
    * <p>Endpoint : {@code GET /sets/{id}}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code id} + réponse JSON)
    */

    public void getSet(Context ctx) {
        String id = ctx.pathParam("id");
        
        if (id == null || id.isBlank()) {
            ctx.status(400).json(ResponseUtil.error("L'identifiant de l'ensemble est requis."));
            return;
        }

        var opt = service.getSet(id);
        if (opt.isEmpty()) {
            ctx.status(404).json(ResponseUtil.error("Ensemble introuvable: " + id));
            return;
        }
        ctx.json(ResponseUtil.ok(opt.get()));
    }

    /**
    * Retourne l'horaire résultant d'un ensemble de cours.
    *
    * <p>Endpoint : {@code GET /sets/{id}/schedule}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code id} + réponse JSON)
    */
    public void getSetSchedule(Context ctx) {
        String id = ctx.pathParam("id");
        
        if (id == null || id.isBlank()) {
            ctx.status(400).json(ResponseUtil.error("L'identifiant de l'ensemble est requis."));
            return;
        }

        var opt = service.getSet(id);
        if (opt.isEmpty()) {
            ctx.status(404).json(ResponseUtil.error("Ensemble introuvable: " + id));
            return;
        }
        ctx.json(ResponseUtil.ok(service.getSetSchedule(id)));
    }

    /**
    * Détecte les conflits d'horaire dans un ensemble de cours (bonus).
    *
    * <p>Endpoint : {@code GET /sets/{id}/conflicts}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code id} + réponse JSON)
    */
    public void getSetConflicts(Context ctx) {
        String id = ctx.pathParam("id");
        
        if (id == null || id.isBlank()) {
            ctx.status(400).json(ResponseUtil.error("L'identifiant de l'ensemble est requis."));
            return;
        }

        var opt = service.getSet(id);
        if (opt.isEmpty()) {
            ctx.status(404).json(ResponseUtil.error("Ensemble introuvable: " + id));
            return;
        }

        var conflicts = service.detectConflicts(id);
        
        if (conflicts.isEmpty()) {
            ctx.json(ResponseUtil.ok(conflicts, "Aucun conflit d'horaire détecté."));
        } else {
            ctx.json(ResponseUtil.ok(conflicts, conflicts.size() + " conflit(s) d'horaire détecté(s)."));
        }
    }
}
