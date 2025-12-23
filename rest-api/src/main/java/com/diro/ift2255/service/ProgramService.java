package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.Program;
import com.diro.ift2255.util.HttpClientApi;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.util.*;

public class ProgramService {

    private final HttpClientApi clientApi;
    private final CourseService courseService;

    // Tentative Planifium (peut ne pas exister)
    private static final String BASE_URL = "https://planifium-api.onrender.com/api/v1/programs";

    // Fallback local: si Planifium programs ne marche pas, on construit un "programme" minimal
    // en s'appuyant sur la liste de cours connue (CSV results / cours DIRO fréquents)
    // -> ici on utilise une liste simple; tu peux l'étendre.
    private static final Map<String, List<String>> PROGRAM_FALLBACK = Map.of(
            "117510", List.of(
                    "IFT1015", "IFT1025", "IFT1065",
                    "IFT2015", "IFT2105", "IFT2255", "IFT2905",
                    "IFT3150", "IFT3225", "IFT3245", "IFT3700"
            )
    );

    public ProgramService(HttpClientApi clientApi, CourseService courseService) {
        this.clientApi = clientApi;
        this.courseService = courseService;
    }

    /**
     * Récupère un programme.
     * Stratégie:
     * 1) Tenter Planifium /programs (3 variantes)
     * 2) Si échec -> fallback local (au moins pour 117510)
     */
    public Optional<Program> getProgram(String programId, String responseLevel) {
        if (programId == null || programId.isBlank()) return Optional.empty();

        String level = (responseLevel == null || responseLevel.isBlank()) ? "min" : responseLevel.trim();
        String pid = programId.trim();

        // 1) /programs/{id}
        try {
            URI uri1 = HttpClientApi.buildUri(
                    BASE_URL + "/" + pid,
                    Map.of("response_level", level, "include_courses_detail", "true")
            );
            Program p = clientApi.get(uri1, Program.class);
            if (p != null && p.getId() != null) return Optional.of(p);
        } catch (RuntimeException ignored) {}

        // 2) /programs?program_id=...
        try {
            Map<String, String> params2 = new HashMap<>();
            params2.put("program_id", pid);
            params2.put("include_courses_detail", "true");
            params2.put("response_level", level);

            URI uri2 = HttpClientApi.buildUri(BASE_URL, params2);
            List<Program> programs = clientApi.get(uri2, new TypeReference<List<Program>>() {});
            if (programs != null && !programs.isEmpty()) return Optional.of(programs.get(0));
        } catch (RuntimeException ignored) {}

        // 3) /programs?programs_list=...
        try {
            Map<String, String> params3 = new HashMap<>();
            params3.put("programs_list", pid);
            params3.put("include_courses_detail", "true");
            params3.put("response_level", level);

            URI uri3 = HttpClientApi.buildUri(BASE_URL, params3);
            List<Program> programs = clientApi.get(uri3, new TypeReference<List<Program>>() {});
            if (programs != null && !programs.isEmpty()) return Optional.of(programs.get(0));
        } catch (RuntimeException ignored) {}

        // -------- Fallback local --------
        List<String> ids = PROGRAM_FALLBACK.get(pid);
        if (ids == null || ids.isEmpty()) return Optional.empty();

        Program fallback = new Program();
        fallback.setId(pid);
        fallback.setName("Programme " + pid + " (fallback local)");

        List<Program.ProgramCourse> pc = new ArrayList<>();
        for (String id : ids) {
            Program.ProgramCourse c = new Program.ProgramCourse();
            c.setId(id);
            c.setName(id);
            pc.add(c);
        }
        fallback.setCourses(pc);
        return Optional.of(fallback);
    }

    /**
     * Cours offerts dans un programme pour un trimestre (H25/A25/E25).
     * Fix majeur: NE PAS mettre schedule_semester en minuscules.
     * On envoie exactement H25/A25/E25.
     */
    public List<Course> getProgramCoursesOfferedInSemester(String programId, String semester, int limit) {
        Optional<Program> programOpt = getProgram(programId, "min");
        if (programOpt.isEmpty() || programOpt.get().getCourses() == null) return List.of();

        String sem = normalizeSemester(semester);
        if (sem == null) return List.of();

        List<String> ids = new ArrayList<>();
        for (Program.ProgramCourse c : programOpt.get().getCourses()) {
            if (c != null && c.getId() != null && !c.getId().isBlank()) {
                ids.add(c.getId().trim().toUpperCase());
            }
        }

        if (ids.isEmpty()) return List.of();

        if (limit <= 0) limit = 200;
        if (ids.size() > limit) ids = ids.subList(0, limit);

        List<Course> offered = new ArrayList<>();
        Map<String, String> qp = new HashMap<>();
        qp.put("include_schedule", "true");

        // IMPORTANT: garder la casse (H25) (ne pas lowerCase)
        qp.put("schedule_semester", sem.toLowerCase());

        for (String id : ids) {
            courseService.getCourseById(id, qp).ifPresent(course -> {
                if (course.getSchedules() != null && !course.getSchedules().isEmpty()) {
                    offered.add(course);
                }
            });
        }

        return offered;
    }

    private String normalizeSemester(String s) {
        if (s == null) return null;
        String sem = s.trim().toUpperCase();
        if (!sem.matches("^[HAE]\\d{2}$")) return null;
        return sem;
    }
}
