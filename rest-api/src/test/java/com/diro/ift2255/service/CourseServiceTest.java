package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.util.HttpClientApi;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CourseService.
 * Les appels HTTP réels sont remplacés par un FakeHttpClientApi pour contrôler les réponses.
 */
public class CourseServiceTest {

    private CourseService courseService;
    private FakeHttpClientApi fakeClient;

    @BeforeEach
    void setup() {
        fakeClient = new FakeHttpClientApi();
        courseService = new CourseService(fakeClient);
    }

    // ========================================================================
    // CU : Recherche de cours
    // ========================================================================

    @Test
    @DisplayName("CU Recherche de cours - getAllCourses retourne une liste de cours quand l'API répond")
    void testGetAllCourses_retourneListeDeCoursQuandApiOk() {
        // ARRANGE
        Course c1 = new Course("IFT1015", "Programmation 1", "Intro à la programmation");
        Course c2 = new Course("IFT2035", "Concepts des langages de programmation", "Cours de C");
        fakeClient.coursesToReturn = List.of(c1, c2);

        Map<String, String> params = Map.of("name", "programmation");

        // ACT
        List<Course> result = courseService.getAllCourses(params);

        // ASSERT
        assertNotNull(result, "La liste retournée ne doit pas être nulle");
        assertEquals(2, result.size(), "La liste doit contenir 2 cours");
        assertEquals("IFT1015", result.get(0).getId());
        assertEquals("IFT2035", result.get(1).getId());
    }

    @Test
    @DisplayName("CU Recherche de cours - getAllCourses retourne une liste vide quand l'API ne renvoie aucun cours")
    void testGetAllCourses_retourneListeVideQuandApiVide() {
        // ARRANGE
        fakeClient.coursesToReturn = List.of(); // aucune donnée

        // ACT
        List<Course> result = courseService.getAllCourses(Collections.emptyMap());

        // ASSERT
        assertNotNull(result, "La liste retournée ne doit pas être nulle");
        assertTrue(result.isEmpty(), "La liste doit être vide quand l'API ne renvoie aucun cours");
    }

    // ========================================================================
    // CU : Voir les détails d'un cours
    // ========================================================================

    @Test
    @DisplayName("CU Détails d'un cours - getCourseById retourne le cours quand il est trouvé")
    void testGetCourseById_retourneCoursQuandTrouve() {
        // ARRANGE
        Course c = new Course("IFT2035", "Concepts des langages de programmation",
                "Historique et concepts des langages de programmation");
        c.setCredits(3.0);
        c.setRequirementText("Préalable : IFT1025");

        fakeClient.courseToReturn = c;
        fakeClient.throwOnGetCourse = false;

        // ACT
        Optional<Course> result = courseService.getCourseById("IFT2035");

        // ASSERT
        assertTrue(result.isPresent(), "Le cours doit être présent");
        assertEquals("IFT2035", result.get().getId());
        assertEquals(3.0, result.get().getCredits());
        assertEquals("Préalable : IFT1025", result.get().getRequirementText());
    }

    // ========================================================================
    // CU : Comparer des cours
    // ========================================================================

    @Test
    @DisplayName("CU Comparer des cours - compareCourses retourne une liste vide si la liste d'IDs est nulle ou vide")
    void testCompareCourses_retourneListeVideQuandListeIdsNulleOuVide() {
        // ARRANGE
        // aucun besoin de configurer le fake client ici : la méthode doit court-circuiter

        // ACT
        List<Course> resultNull = courseService.compareCourses(null);
        List<Course> resultVide = courseService.compareCourses(List.of());

        // ASSERT
        assertNotNull(resultNull, "Le résultat pour une liste nulle ne doit pas être nul");
        assertTrue(resultNull.isEmpty(), "Le résultat pour une liste nulle doit être vide");

        assertNotNull(resultVide, "Le résultat pour une liste vide ne doit pas être nul");
        assertTrue(resultVide.isEmpty(), "Le résultat pour une liste vide doit être vide");
    }

    // ========================================================================
    // Fake client HTTP pour isoler CourseService de l'API réelle
    // ========================================================================

    /**
     * Implémentation factice de HttpClientApi utilisée uniquement pour les tests.
     * Elle permet de contrôler les valeurs retournées par les appels GET.
     */
    private static class FakeHttpClientApi extends HttpClientApi {

        List<Course> coursesToReturn = new ArrayList<>();
        Course courseToReturn = null;
        boolean throwOnGetCourse = false;

        @Override
        public <T> T get(URI uri, Class<T> clazz) {
            if (throwOnGetCourse) {
                throw new RuntimeException("Simulated API error for get(URI, Class)");
            }
            // On suppose ici que le seul type demandé est Course
            @SuppressWarnings("unchecked")
            T value = (T) courseToReturn;
            return value;
        }

        @Override
        public <T> T get(URI uri, TypeReference<T> typeRef) {
            // On suppose ici que le seul type demandé est List<Course>
            @SuppressWarnings("unchecked")
            T value = (T) coursesToReturn;
            return value;
        }
    }
}
