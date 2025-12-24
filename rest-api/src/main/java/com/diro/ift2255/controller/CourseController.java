package com.diro.ift2255.controller;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.EligibilityResult;
import com.diro.ift2255.service.AcademicResultService;
import com.diro.ift2255.service.CompareService;
import com.diro.ift2255.service.CourseService;
import com.diro.ift2255.service.ProgramService;
import com.diro.ift2255.util.ResponseUtil;
import io.javalin.http.Context;

import java.util.*;

public class CourseController {

    private final CourseService service;
    private final AcademicResultService resultsService;
    private final CompareService compareService;
    private final ProgramService programService;

    public CourseController(CourseService service,
                            AcademicResultService resultsService,
                            CompareService compareService,
                            ProgramService programService) {
        this.service = service;
        this.resultsService = resultsService;
        this.compareService = compareService;
        this.programService = programService;
    }

    // Validation type: IFT2255 (3 lettres + 4 chiffres)
    private boolean validateCourseId(String courseId) {
        return courseId != null && courseId.trim().matches("(?i)^[A-Z]{3}\\d{4}$");
    }

    /**
     * CU1 - Recherche / liste de cours avec filtres Planifium
     * Ex:
     *  GET /courses?name=logiciel
     *  GET /courses?description=java
     *  GET /courses?courses_sigle=ift1015,ift1025
     *  GET /courses?sigle_prefix=IFT  (recherche par préfixe, ex: tous les IFT*)
     */
    public void getAllCourses(Context ctx) {
        // Cas spécial: recherche par préfixe de sigle (ex: IFT → tous les IFT*)
        String siglePrefix = ctx.queryParam("sigle_prefix");
        if (siglePrefix != null && !siglePrefix.isBlank()) {
            String prefix = siglePrefix.trim().toUpperCase();
            if (!prefix.matches("^[A-Z]{2,3}$")) {
                ctx.status(400).json(ResponseUtil.error(
                        "Le préfixe de sigle doit contenir 2 ou 3 lettres (ex: IFT, MAT, PHY)."));
                return;
            }
            Map<String, String> queryParams = extractQueryParams(ctx);
            queryParams.remove("sigle_prefix"); // Ne pas envoyer à Planifium
            List<Course> courses = service.searchBySiglePrefix(prefix, queryParams);
            
            // Message informatif si préfixe non supporté (liste vide)
            if (courses.isEmpty()) {
                ctx.json(ResponseUtil.ok(courses, 
                    "Aucun cours trouvé pour le préfixe '" + prefix + "'. Préfixes supportés: IFT, MAT, STT, PHY."));
                return;
            }
            ctx.json(ResponseUtil.ok(courses));
            return;
        }

        // Cas normal: recherche Planifium (name, description, courses_sigle)
        Map<String, String> queryParams = extractQueryParams(ctx);
        List<Course> courses = service.getAllCourses(queryParams);
        ctx.json(ResponseUtil.ok(courses));
    }

    /**
     * cours offerts pour un trimestre donné (global)
     * ex:
     *  GET /courses/offered?semester=H25
     *  GET /courses/offered?semester=H25&programId=117510
     *  GET /courses/offered?semester=H25&limit=50
     */
    public void getCoursesOfferedBySemester(Context ctx) {
        String semester = ctx.queryParam("semester");

        // Validation du trimestre 
        if (semester == null || semester.isBlank()) {
            ctx.status(400).json(ResponseUtil.error("Le paramètre 'semester' est requis (ex: H25, A24, E24)."));
            return;
        }

        String semNormalized = semester.trim().toUpperCase();
        if (!semNormalized.matches("^[HAE]\\d{2}$")) {
            ctx.status(400).json(ResponseUtil.error("Format de trimestre invalide. Utilisez H25, A24, E24, etc."));
            return;
        }

        // Paramètre optionnel: programId
        String programId = ctx.queryParam("programId");

        // Paramètre optionnel: limit
        int limit = 100;
        String limitParam = ctx.queryParam("limit");
        if (limitParam != null && !limitParam.isBlank()) {
            try {
                limit = Integer.parseInt(limitParam.trim());
                if (limit < 1 || limit > 500) {
                    ctx.status(400).json(ResponseUtil.error("Le paramètre 'limit' doit être entre 1 et 500."));
                    return;
                }
            } catch (NumberFormatException e) {
                ctx.status(400).json(ResponseUtil.error("Le paramètre 'limit' doit être un entier."));
                return;
            }
        }

        List<Course> courses;

        if (programId != null && !programId.isBlank()) {
            // Filtrer par programme
            courses = programService.getProgramCoursesOfferedInSemester(programId.trim(), semNormalized, limit);
        } else {
            // Recherche globale via Planifium avec schedule
            courses = service.getCoursesOfferedBySemester(semNormalized, limit);
        }

        ctx.json(ResponseUtil.ok(courses));
    }

    /**
     * CU2 - Détails d'un cours
     * Ex:
     *  GET /courses/IFT2255
     *  GET /courses/IFT2255?include_schedule=true&schedule_semester=H25
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
     * CU3 - Comparer des cours (ancienne version : renvoie juste les cours Planifium)
     * Ex:
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
     * NEW - Comparaison "réelle" : Planifium (nom) + Avis + CSV
     * Ex:
     *  GET /courses/compare-full?ids=IFT2255,IFT1025
     */
    public void compareCoursesFull(Context ctx) {
        String idsParam = ctx.queryParam("ids");

        if (idsParam == null || idsParam.isBlank()) {
            ctx.status(400).json(ResponseUtil.error("Le paramètre 'ids' est requis (ex: ids=IFT2255,IFT1025)."));
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

        // Retourne une liste de CompareItem (DTO) via CompareService
        ctx.json(ResponseUtil.ok(compareService.compare(ids)));
    }

    /**
     * Vérifier l’éligibilité d’un étudiant à un cours selon les prérequis + cours complétés.
     * Ex:
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
                if (cycle < 1 || cycle > 3) {
                    ctx.status(400).json(ResponseUtil.error(
                            "Le paramètre cycle doit être 1 (baccalauréat), 2 (maîtrise) ou 3 (doctorat)."));
                    return;
                }
            } catch (NumberFormatException e) {
                ctx.status(400).json(ResponseUtil.error(
                        "Le paramètre cycle doit être un entier: 1 (baccalauréat), 2 (maîtrise) ou 3 (doctorat)."));
                return;
            }
        }

        EligibilityResult result = service.checkEligibility(id, completed, cycle);
        ctx.json(ResponseUtil.ok(result));
    }

    /**
     * Résultats académiques agrégés (CSV)
     * Ex:
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
