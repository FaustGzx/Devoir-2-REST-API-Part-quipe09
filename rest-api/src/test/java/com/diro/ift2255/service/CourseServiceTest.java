package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.EligibilityResult;
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
    @DisplayName("CU Recherche - getAllCourses retourne une liste quand l'API répond")
    void testGetAllCourses_retourneListeDeCoursQuandApiOk() {
        Course c1 = new Course("IFT1015", "Programmation 1", "Intro à la programmation");
        Course c2 = new Course("IFT2035", "Concepts des langages de programmation", "Cours de C");
        fakeClient.coursesToReturn = List.of(c1, c2);

        Map<String, String> params = Map.of("name", "programmation");

        List<Course> result = courseService.getAllCourses(params);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("IFT1015", result.get(0).getId());
        assertEquals("IFT2035", result.get(1).getId());
    }

    @Test
    @DisplayName("CU Recherche - retourne liste vide si aucun cours trouvé")
    void testGetAllCourses_retourneListeVideQuandApiVide() {
        fakeClient.coursesToReturn = List.of();

        List<Course> result = courseService.getAllCourses(Collections.emptyMap());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("CU Recherche - getCourseById retourne le bon ID de cours")
    void testGetAllCourses_filtreParNom() {

        Course c = new Course("IFT1015", "Programmation 1", "Intro à la programmation");
        fakeClient.courseToReturn = c;
        fakeClient.throwOnGetCourse = false;

        Optional<Course> result = courseService.getCourseById("IFT1015");

        assertTrue(result.isPresent());
        assertEquals("IFT1015", result.get().getId());
    }

    

    // ========================================================================
    // CU : Voir les détails d'un cours
    // ========================================================================

    @Test
    @DisplayName("CU Détail - getCourseById retourne le cours si trouvé")
    void testGetCourseById_retourneCoursQuandTrouve() {
        Course c = new Course("IFT2035", "Concepts des langages de programmation",
                "Historique et concepts des langages de programmation");
        c.setCredits(3.0);
        c.setRequirementText("Préalable : IFT1025");

        fakeClient.courseToReturn = c;
        fakeClient.throwOnGetCourse = false;

        Optional<Course> result = courseService.getCourseById("IFT2035");

        assertTrue(result.isPresent());
        assertEquals("IFT2035", result.get().getId());
        assertEquals(3.0, result.get().getCredits());
        assertEquals("Préalable : IFT1025", result.get().getRequirementText());
    }

    @Test
    @DisplayName("CU Détail - retourne Optional.empty() quand l'ID est vide")
    void testGetCourseById_idInvalideRetourneEmpty() {
        fakeClient.throwOnGetCourse = true; // Simule une erreur API

        Optional<Course> result = courseService.getCourseById("   ");

        assertTrue(result.isEmpty());
    }
    @Test
    @DisplayName("CU Détail - retourne Optional.empty() lorsque le cours n'existe pas")
    void testGetCourseById_coursInexistantRetourneEmpty() {

        fakeClient.courseToReturn = null;  
        fakeClient.throwOnGetCourse = false;

        Optional<Course> result = courseService.getCourseById("IFT9999");

        assertTrue(result.isEmpty());
    }




    // ========================================================================
    // CU : Éligibilité à un cours
    // ========================================================================

    @Test
    @DisplayName("CU Éligibilité - étudiant éligible quand tous les prérequis sont remplis")
    void testCheckEligibility_etudiantEligibleQuandTousPrerequisOK() {
        Course c = new Course("IFT2035", "Concepts des langages de programmation", null);
        c.setPrerequisiteCourses(List.of("IFT1025", "IFT1015"));

        fakeClient.courseToReturn = c;
        fakeClient.throwOnGetCourse = false;

        List<String> completed = List.of("IFT1025", "IFT1015");

        EligibilityResult result = courseService.checkEligibility("IFT2035", completed);

        assertTrue(result.isEligible());
        assertTrue(result.getMissingPrerequisites().isEmpty());
    }

    @Test
    @DisplayName("CU Éligibilité - étudiant non éligible si prérequis manquants")
    void testCheckEligibility_nonEligibleQuandPrerequisManquants() {
        Course c = new Course("IFT2035", "Concepts des langages de programmation", null);
        c.setPrerequisiteCourses(List.of("IFT1025", "IFT1015"));

        fakeClient.courseToReturn = c;
        fakeClient.throwOnGetCourse = false;

        List<String> completed = List.of("IFT1025");

        EligibilityResult result = courseService.checkEligibility("IFT2035", completed);

        assertFalse(result.isEligible());
        assertEquals(List.of("IFT1015"), result.getMissingPrerequisites());
    }

    // ========================================================================
    // CU : Comparer des cours
    // ========================================================================

    @Test
    @DisplayName("CU Comparer - retourne liste vide quand la liste d'IDs est vide")
    void testCompareCourses_retourneListeVideQuandIdsVides() {
        Course c1 = new Course("IFT1015", "Programmation 1", "Intro");
        c1.setCredits(3.0);
        Course c2 = new Course("IFT2035", "Concepts des langages de programmation", "Cours de C");
        // Simule la réponse de l'API
        fakeClient.coursesToReturn = List.of(c1, c2);

        List<String> ids = List.of("IFT1015", "IFT2035");

        Map<String, String> params = Map.of(
            "include_schedule", "true",
            "schedule_semester", "A25"
            );
         // ACT
         List<Course> result = courseService.compareCourses(ids, params);

         // ASSERT
         assertNotNull(result);
    }

    @Test
    @DisplayName("CU Comparer - ignore les IDs nuls ou vides et ne retourne que les cours valides")
    void testCompareCourses_ignoreIdsInvalides() {
        Course c = new Course("IFT1015", "Programmation 1", "Intro");
        c.setCredits(3.0);

        
        fakeClient.courseToReturn = c;
        fakeClient.throwOnGetCourse = false;

        
        List<String> ids = Arrays.asList("   ", null, "IFT1015");

        List<Course> result = courseService.compareCourses(ids);

        assertNotNull(result);
        assertEquals(1, result.size(), "Un seul ID valide doit être pris en compte");
        assertEquals("IFT1015", result.get(0).getId());
    }

    @Test
    @DisplayName("CU Comparer - retourne liste vide quand l'API lève une exception")
    void testCompareCourses_retourneListeVideQuandApiException() {
        // Fake client：在 get(URI, Class) 时抛 RuntimeException
        fakeClient.throwOnGetCourse = true;

        List<String> ids = List.of("IFT1015", "IFT2035");

        List<Course> result = courseService.compareCourses(ids);

        assertNotNull(result);
        assertTrue(result.isEmpty(),
                "En cas d'erreur API, compareCourses doit retourner une liste vide et ne pas planter");
    }



    // ========================================================================
    // Fake client HTTP pour isoler CourseService de l'API réelle
    // ========================================================================

    private static class FakeHttpClientApi extends HttpClientApi {

        List<Course> coursesToReturn = new ArrayList<>();
        Course courseToReturn = null;
        boolean throwOnGetCourse = false;

        @Override
        public <T> T get(URI uri, Class<T> clazz) {
            if (throwOnGetCourse) {
                throw new RuntimeException("Simulated API error for get(URI, Class)");
            }
            @SuppressWarnings("unchecked")
            T value = (T) courseToReturn;
            return value;
        }

        @Override
        public <T> T get(URI uri, TypeReference<T> typeRef) {
            @SuppressWarnings("unchecked")
            T value = (T) coursesToReturn;
            return value;
        }
    }
}
