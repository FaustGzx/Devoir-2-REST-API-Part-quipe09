package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.CourseSet;
import com.diro.ift2255.util.HttpClientApi;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CourseSetService.
 * Couvre la création d'ensembles de cours et la détection de conflits d'horaire.
 */
public class CourseSetServiceTest {

    private CourseSetService courseSetService;
    private FakeCourseService fakeCourseService;

    @BeforeEach
    void setup() {
        fakeCourseService = new FakeCourseService();
        courseSetService = new CourseSetService(fakeCourseService);
    }

    // ========================================================================
    // CU : Créer un ensemble de cours
    // ========================================================================

    @Test
    @DisplayName("CU Ensemble - createSet crée un ensemble valide")
    void testCreateSet_ensembleValide() {
        List<String> courseIds = List.of("IFT1015", "IFT2255", "IFT2015");

        Optional<CourseSet> result = courseSetService.createSet("H25", courseIds);

        assertTrue(result.isPresent(), "L'ensemble doit être créé");
        assertEquals("H25", result.get().getSemester());
        assertEquals(3, result.get().getCourseIds().size());
        assertNotNull(result.get().getId(), "L'ID doit être généré");
    }

    @Test
    @DisplayName("CU Ensemble - createSet refuse plus de 6 cours")
    void testCreateSet_refusePlusDe6Cours() {
        List<String> courseIds = List.of("IFT1015", "IFT1025", "IFT2015", "IFT2255", 
                                         "IFT3150", "IFT3225", "IFT3245");

        Optional<CourseSet> result = courseSetService.createSet("H25", courseIds);

        assertTrue(result.isEmpty(), "L'ensemble ne doit pas être créé avec plus de 6 cours");
    }

    @Test
    @DisplayName("CU Ensemble - createSet refuse un trimestre invalide")
    void testCreateSet_refuseTrimestreInvalide() {
        List<String> courseIds = List.of("IFT1015", "IFT2255");

        Optional<CourseSet> resultNull = courseSetService.createSet(null, courseIds);
        Optional<CourseSet> resultInvalid = courseSetService.createSet("INVALID", courseIds);
        Optional<CourseSet> resultX99 = courseSetService.createSet("X99", courseIds);

        assertTrue(resultNull.isEmpty(), "Trimestre null doit être refusé");
        assertTrue(resultInvalid.isEmpty(), "Trimestre 'INVALID' doit être refusé");
        assertTrue(resultX99.isEmpty(), "Trimestre 'X99' doit être refusé");
    }

    @Test
    @DisplayName("CU Ensemble - createSet refuse un sigle de cours invalide")
    void testCreateSet_refuseSigleInvalide() {
        List<String> courseIds = List.of("IFT1015", "INVALID", "IFT2255");

        Optional<CourseSet> result = courseSetService.createSet("H25", courseIds);

        assertTrue(result.isEmpty(), "L'ensemble ne doit pas être créé avec un sigle invalide");
    }

    @Test
    @DisplayName("CU Ensemble - createSet refuse une liste vide")
    void testCreateSet_refuseListeVide() {
        Optional<CourseSet> resultEmpty = courseSetService.createSet("H25", List.of());
        Optional<CourseSet> resultNull = courseSetService.createSet("H25", null);

        assertTrue(resultEmpty.isEmpty(), "Liste vide doit être refusée");
        assertTrue(resultNull.isEmpty(), "Liste null doit être refusée");
    }

    @Test
    @DisplayName("CU Ensemble - getSet retourne l'ensemble créé")
    void testGetSet_retourneEnsembleCree() {
        List<String> courseIds = List.of("IFT1015", "IFT2255");
        Optional<CourseSet> created = courseSetService.createSet("A24", courseIds);
        
        assertTrue(created.isPresent());
        String setId = created.get().getId();

        Optional<CourseSet> retrieved = courseSetService.getSet(setId);

        assertTrue(retrieved.isPresent());
        assertEquals(setId, retrieved.get().getId());
        assertEquals("A24", retrieved.get().getSemester());
    }

    @Test
    @DisplayName("CU Ensemble - getSet retourne vide pour ID inexistant")
    void testGetSet_retourneVidePourIdInexistant() {
        Optional<CourseSet> result = courseSetService.getSet("id-inexistant");

        assertTrue(result.isEmpty());
    }

    // ========================================================================
    // BONUS : Détection des conflits d'horaire
    // ========================================================================

    @Test
    @DisplayName("BONUS Conflits - detectConflicts retourne liste vide si pas de conflit")
    void testDetectConflicts_pasDeConflit() {
        // Créer un ensemble
        List<String> courseIds = List.of("IFT1015", "IFT2255");
        Optional<CourseSet> created = courseSetService.createSet("H25", courseIds);
        assertTrue(created.isPresent());

        // Configurer les cours sans conflit (jours différents)
        Course c1 = createCourseWithSchedule("IFT1015", "Lu", "08:30", "10:30");
        Course c2 = createCourseWithSchedule("IFT2255", "Ma", "08:30", "10:30");
        fakeCourseService.coursesToReturn.put("IFT1015", c1);
        fakeCourseService.coursesToReturn.put("IFT2255", c2);

        var conflicts = courseSetService.detectConflicts(created.get().getId());

        assertTrue(conflicts.isEmpty(), "Pas de conflit attendu pour des jours différents");
    }

    @Test
    @DisplayName("BONUS Conflits - detectConflicts détecte un conflit de chevauchement")
    void testDetectConflicts_detecteChevauchement() {
        List<String> courseIds = List.of("IFT1015", "IFT2255");
        Optional<CourseSet> created = courseSetService.createSet("H25", courseIds);
        assertTrue(created.isPresent());

        // Même jour, heures qui se chevauchent
        Course c1 = createCourseWithSchedule("IFT1015", "Lu", "08:30", "10:30");
        Course c2 = createCourseWithSchedule("IFT2255", "Lu", "09:30", "11:30");
        fakeCourseService.coursesToReturn.put("IFT1015", c1);
        fakeCourseService.coursesToReturn.put("IFT2255", c2);

        var conflicts = courseSetService.detectConflicts(created.get().getId());

        assertFalse(conflicts.isEmpty(), "Un conflit doit être détecté");
        assertEquals(1, conflicts.size());
    }

    @Test
    @DisplayName("BONUS Conflits - detectConflicts retourne vide pour ensemble inexistant")
    void testDetectConflicts_ensembleInexistant() {
        var conflicts = courseSetService.detectConflicts("id-inexistant");

        assertTrue(conflicts.isEmpty());
    }

    @Test
    @DisplayName("BONUS Conflits - detectConflicts détecte conflit sur activité multi-jours")
    void testDetectConflicts_activiteMultiJours() {
        List<String> courseIds = List.of("IFT1015", "IFT2255");
        Optional<CourseSet> created = courseSetService.createSet("H25", courseIds);
        assertTrue(created.isPresent());

        // c1 a une activité sur ["Lu", "Me"] 
        // c2 a une activité sur ["Me"] seulement
        // → conflit attendu sur "Me"
        Course c1 = createCourseWithSchedule("IFT1015", List.of("Lu", "Me"), "08:30", "10:30");
        Course c2 = createCourseWithSchedule("IFT2255", List.of("Me"), "09:00", "11:00");
        fakeCourseService.coursesToReturn.put("IFT1015", c1);
        fakeCourseService.coursesToReturn.put("IFT2255", c2);

        var conflicts = courseSetService.detectConflicts(created.get().getId());

        assertFalse(conflicts.isEmpty(), "Un conflit doit être détecté sur Me");
        // Vérifier que le conflit est bien sur Me
        boolean foundMeConflict = conflicts.stream()
                .anyMatch(c -> c.getDay1().equalsIgnoreCase("Me") || c.getDay2().equalsIgnoreCase("Me"));
        assertTrue(foundMeConflict, "Le conflit doit être sur le jour Me");
    }

    @Test
    @DisplayName("BONUS Conflits - pas de conflit quand heures se touchent sans chevaucher")
    void testDetectConflicts_heuresContiguesSansChevauchement() {
        List<String> courseIds = List.of("IFT1015", "IFT2255");
        Optional<CourseSet> created = courseSetService.createSet("H25", courseIds);
        assertTrue(created.isPresent());

        // c1: 08:30–10:30, c2: 10:30–11:30 (se touchent mais ne chevauchent pas)
        Course c1 = createCourseWithSchedule("IFT1015", List.of("Lu"), "08:30", "10:30");
        Course c2 = createCourseWithSchedule("IFT2255", List.of("Lu"), "10:30", "11:30");
        fakeCourseService.coursesToReturn.put("IFT1015", c1);
        fakeCourseService.coursesToReturn.put("IFT2255", c2);

        var conflicts = courseSetService.detectConflicts(created.get().getId());

        assertTrue(conflicts.isEmpty(), 
            "Pas de conflit attendu: 10:30 fin de c1 = 10:30 début de c2 (pas de chevauchement)");
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private Course createCourseWithSchedule(String id, String day, String startTime, String endTime) {
        return createCourseWithSchedule(id, List.of(day), startTime, endTime);
    }

    private Course createCourseWithSchedule(String id, List<String> days, String startTime, String endTime) {
        Course course = new Course(id, "Test Course", "Description");
        
        // Créer l'activité avec plusieurs jours
        Map<String, Object> activity = new HashMap<>();
        activity.put("days", days);
        activity.put("start_time", startTime);
        activity.put("end_time", endTime);

        // Créer le volet avec l'activité
        Map<String, Object> volet = new HashMap<>();
        volet.put("activities", List.of(activity));
        volet.put("name", "TH");

        // Créer la section
        Course.Section section = new Course.Section();
        section.setName("A");
        section.setVolets(List.of(volet));

        // Créer le schedule
        Course.Schedule schedule = new Course.Schedule();
        schedule.setName(course.getName());
        schedule.setSections(List.of(section));

        course.setSchedules(List.of(schedule));
        return course;
    }

    // ========================================================================
    // Fake CourseService pour les tests
    // ========================================================================

    private static class FakeCourseService extends CourseService {
        Map<String, Course> coursesToReturn = new HashMap<>();

        FakeCourseService() {
            super(new FakeHttpClientApi());
        }

        @Override
        public Optional<Course> getCourseById(String courseId, Map<String, String> queryParams) {
            return Optional.ofNullable(coursesToReturn.get(courseId.toUpperCase()));
        }
    }

    private static class FakeHttpClientApi extends HttpClientApi {
        @Override
        public <T> T get(URI uri, Class<T> clazz) {
            return null;
        }

        @Override
        public <T> T get(URI uri, TypeReference<T> typeRef) {
            return null;
        }
    }
}
