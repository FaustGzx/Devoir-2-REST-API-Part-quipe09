package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.CourseSet;

import java.util.*;
/**
 * Service responsable de la gestion des ensembles de cours.
 *
 * <p>Ce service permet de créer des ensembles de cours,
 * de calculer l’horaire résultant pour un trimestre donné
 * et de détecter les conflits d’horaire éventuels.</p>
 */

public class CourseSetService {

    private final CourseService courseService;
    private final Map<String, CourseSet> sets = new HashMap<>();

    public CourseSetService(CourseService courseService) {
        this.courseService = courseService;
    }

    public Optional<CourseSet> createSet(String semester, List<String> courseIds) {
        String sem = normalizeSemester(semester);
        if (sem == null) return Optional.empty();

        if (courseIds == null) return Optional.empty();
        List<String> cleaned = courseIds.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (cleaned.isEmpty() || cleaned.size() > 6) return Optional.empty();
        for (String id : cleaned) {
            if (!id.matches("^[A-Z]{3}\\d{4}$")) return Optional.empty();
        }

        String id = UUID.randomUUID().toString();
        CourseSet set = new CourseSet(id, sem, cleaned);
        sets.put(id, set);
        return Optional.of(set);
    }

    public Optional<CourseSet> getSet(String id) {
        return Optional.ofNullable(sets.get(id));
    }

    // Horaire résultant: retourne les cours avec schedules filtrés par trimestre
    public List<Course> getSetSchedule(String setId) {
        Optional<CourseSet> opt = getSet(setId);
        if (opt.isEmpty()) return List.of();

        CourseSet set = opt.get();
        Map<String, String> qp = new HashMap<>();
        qp.put("include_schedule", "true");
        qp.put("schedule_semester", set.getSemester().toLowerCase());

        List<Course> out = new ArrayList<>();
        for (String cid : set.getCourseIds()) {
            courseService.getCourseById(cid, qp).ifPresent(out::add);
        }
        return out;
    }

    // ========================================================================
    // BONUS: Détection des conflits d'horaire
    // ========================================================================

    /**
     * Détecte les conflits d'horaire dans un ensemble de cours.
     * Un conflit existe quand deux cours ont des activités au même moment.
     * 
     * Améliorations:
     * - Gère les activités multi-jours (crée un slot par jour)
     * - Inclut le type d'activité (TH/TP/LAB) dans le conflit
     * - Dédoublonne les conflits identiques
     */
    public List<ScheduleConflict> detectConflicts(String setId) {
        List<Course> courses = getSetSchedule(setId);
        if (courses.isEmpty()) return List.of();

        List<ActivitySlot> allSlots = new ArrayList<>();
        int activitiesIgnored = 0;

        // Extraire tous les créneaux horaires
        for (Course course : courses) {
            if (course.getSchedules() == null) continue;
            
            for (Course.Schedule schedule : course.getSchedules()) {
                if (schedule.getSections() == null) continue;
                
                for (Course.Section section : schedule.getSections()) {
                    if (section.getVolets() == null) continue;
                    
                    for (Map<String, Object> volet : section.getVolets()) {
                        // Récupérer le type d'activité (TH, TP, LAB, etc.)
                        String voletName = (String) volet.getOrDefault("name", "");
                        
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> activities = (List<Map<String, Object>>) volet.get("activities");
                        if (activities == null) continue;
                        
                        for (Map<String, Object> activity : activities) {
                            List<ActivitySlot> slots = parseActivitySlots(
                                course.getId(), section.getName(), voletName, activity);
                            if (slots.isEmpty()) {
                                activitiesIgnored++;
                            } else {
                                allSlots.addAll(slots);
                            }
                        }
                    }
                }
            }
        }

        // Log si des activités ont été ignorées (structure Planifium fragile)
        if (activitiesIgnored > 0) {
            System.err.println("[CourseSetService] " + activitiesIgnored + " activités ignorées (données manquantes/invalides)");
        }

        // Comparer chaque paire de créneaux pour détecter les conflits
        // Utiliser un Set pour dédoublonner
        Set<String> seenConflicts = new HashSet<>();
        List<ScheduleConflict> conflicts = new ArrayList<>();
        
        for (int i = 0; i < allSlots.size(); i++) {
            for (int j = i + 1; j < allSlots.size(); j++) {
                ActivitySlot s1 = allSlots.get(i);
                ActivitySlot s2 = allSlots.get(j);

                // Ne pas comparer les créneaux du même cours
                if (s1.courseId.equals(s2.courseId)) continue;

                if (slotsOverlap(s1, s2)) {
                    // Créer une clé unique pour dédoublonner
                    String conflictKey = generateConflictKey(s1, s2);
                    
                    if (!seenConflicts.contains(conflictKey)) {
                        seenConflicts.add(conflictKey);
                        conflicts.add(new ScheduleConflict(
                                s1.courseId, s1.section, s1.activityType, s1.day, s1.startTime, s1.endTime,
                                s2.courseId, s2.section, s2.activityType, s2.day, s2.startTime, s2.endTime
                        ));
                    }
                }
            }
        }

        return conflicts;
    }

    /**
     * Parse une activité et retourne UN SLOT PAR JOUR (gère les activités multi-jours).
     */
    private List<ActivitySlot> parseActivitySlots(String courseId, String section, 
                                                   String activityType, Map<String, Object> activity) {
        List<ActivitySlot> slots = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<String> days = (List<String>) activity.get("days");
            String startTime = (String) activity.get("start_time");
            String endTime = (String) activity.get("end_time");

            if (days == null || days.isEmpty() || startTime == null || endTime == null) {
                return slots;
            }

            // Créer un slot pour CHAQUE jour (fix multi-jours)
            for (String day : days) {
                slots.add(new ActivitySlot(courseId, section, activityType, day, startTime, endTime));
            }
        } catch (Exception e) {
            // Structure Planifium fragile - on ignore silencieusement
        }
        return slots;
    }

    /**
     * Génère une clé unique pour un conflit (pour dédoublonnage).
     * La clé est ordonnée pour que (A,B) == (B,A).
     */
    private String generateConflictKey(ActivitySlot s1, ActivitySlot s2) {
        String key1 = s1.courseId + "|" + s1.section + "|" + s1.activityType + "|" + s1.day + "|" + s1.startTime;
        String key2 = s2.courseId + "|" + s2.section + "|" + s2.activityType + "|" + s2.day + "|" + s2.startTime;
        // Ordonner pour que (A,B) == (B,A)
        return key1.compareTo(key2) < 0 ? key1 + ":" + key2 : key2 + ":" + key1;
    }

    private boolean slotsOverlap(ActivitySlot s1, ActivitySlot s2) {
        // Même jour requis
        if (!s1.day.equalsIgnoreCase(s2.day)) return false;

        // Convertir les heures en minutes pour comparaison
        int s1Start = timeToMinutes(s1.startTime);
        int s1End = timeToMinutes(s1.endTime);
        int s2Start = timeToMinutes(s2.startTime);
        int s2End = timeToMinutes(s2.endTime);

        // Chevauchement: s1 commence avant que s2 finisse ET s2 commence avant que s1 finisse
        return s1Start < s2End && s2Start < s1End;
    }

    private int timeToMinutes(String time) {
        try {
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    private String normalizeSemester(String s) {
        if (s == null) return null;
        String sem = s.trim().toUpperCase();
        if (!sem.matches("^[HAE]\\d{2}$")) return null;
        return sem;
    }

    // DTO interne pour représenter un créneau horaire
    private static class ActivitySlot {
        String courseId;
        String section;
        String activityType;  // TH, TP, LAB, etc.
        String day;
        String startTime;
        String endTime;

        ActivitySlot(String courseId, String section, String activityType, 
                     String day, String startTime, String endTime) {
            this.courseId = courseId;
            this.section = section;
            this.activityType = activityType != null ? activityType : "";
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    // DTO public pour les conflits détectés
    public static class ScheduleConflict {
        private final String course1;
        private final String section1;
        private final String activityType1;
        private final String day1;
        private final String startTime1;
        private final String endTime1;
        private final String course2;
        private final String section2;
        private final String activityType2;
        private final String day2;
        private final String startTime2;
        private final String endTime2;

        public ScheduleConflict(String course1, String section1, String activityType1,
                               String day1, String startTime1, String endTime1,
                               String course2, String section2, String activityType2,
                               String day2, String startTime2, String endTime2) {
            this.course1 = course1;
            this.section1 = section1;
            this.activityType1 = activityType1;
            this.day1 = day1;
            this.startTime1 = startTime1;
            this.endTime1 = endTime1;
            this.course2 = course2;
            this.section2 = section2;
            this.activityType2 = activityType2;
            this.day2 = day2;
            this.startTime2 = startTime2;
            this.endTime2 = endTime2;
        }

        public String getCourse1() { return course1; }
        public String getSection1() { return section1; }
        public String getActivityType1() { return activityType1; }
        public String getDay1() { return day1; }
        public String getStartTime1() { return startTime1; }
        public String getEndTime1() { return endTime1; }
        public String getCourse2() { return course2; }
        public String getSection2() { return section2; }
        public String getActivityType2() { return activityType2; }
        public String getDay2() { return day2; }
        public String getStartTime2() { return startTime2; }
        public String getEndTime2() { return endTime2; }

        public String getDescription() {
            String type1 = activityType1.isEmpty() ? "" : " (" + activityType1 + ")";
            String type2 = activityType2.isEmpty() ? "" : " (" + activityType2 + ")";
            // Utiliser timeToMinutes pour calculer min/max proprement (évite les bugs avec compareTo sur strings)
            String overlapStart = timeToMinutes(startTime1) < timeToMinutes(startTime2) ? startTime1 : startTime2;
            String overlapEnd = timeToMinutes(endTime1) > timeToMinutes(endTime2) ? endTime1 : endTime2;
            return String.format("Conflit: %s section %s%s et %s section %s%s le %s de %s à %s",
                    course1, section1, type1, course2, section2, type2, day1, overlapStart, overlapEnd);
        }

        private static int timeToMinutes(String time) {
            try {
                String[] parts = time.split(":");
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
