package com.diro.ift2255.config;

import com.diro.ift2255.controller.*;
import com.diro.ift2255.service.*;
import com.diro.ift2255.util.HttpClientApi;
import io.javalin.Javalin;

public class Routes {

    public static void register(Javalin app) {
        // Instancier les dépendances partagées 1 seule fois
        HttpClientApi api = new HttpClientApi();

        // Services communs
        CourseService courseService = new CourseService(api);
        AcademicResultService academicResultService = new AcademicResultService("historique_cours_prog_117510.csv");
        ReviewService reviewService = new ReviewService("data/reviews.json");
        CompareService compareService = new CompareService(courseService, reviewService, academicResultService);


        ProgramService programService = new ProgramService(api, courseService);
        ProgramController programController = new ProgramController(programService);

        CourseSetService courseSetService = new CourseSetService(courseService);
        CourseSetController courseSetController = new CourseSetController(courseSetService);

        UserService userService = new UserService();
        UserController userController = new UserController(userService);

        // CourseController reçoit aussi ProgramService pour /courses/offered
        CourseController courseController = new CourseController(courseService, academicResultService, compareService, programService);

        ReviewController reviewController = new ReviewController(reviewService);

        // Enregistrer les routes
        registerUserRoutes(app, userController);
        registerCourseRoutes(app, courseController);
        registerProgramRoutes(app, programController);
        registerReviewRoutes(app, reviewController);
        registerCourseSetRoutes(app, courseSetController);
    }

    // -----------------------------
    // USERS
    // -----------------------------
    private static void registerUserRoutes(Javalin app, UserController userController) {
        app.get("/users", userController::getAllUsers);
        app.get("/users/{id}", userController::getUserById);
        app.post("/users", userController::createUser);
        app.put("/users/{id}", userController::updateUser);
        app.delete("/users/{id}", userController::deleteUser);
    }

    // -----------------------------
    // COURSES
    // -----------------------------
    private static void registerCourseRoutes(Javalin app, CourseController courseController) {
        app.get("/courses", courseController::getAllCourses);

        // cours offerts pour un trimestre donné (global, optionnel programId)
        app.get("/courses/offered", courseController::getCoursesOfferedBySemester);

        // comparaison "simple" (ancienne) : renvoie juste les cours Planifium
        app.get("/courses/comparer", courseController::compareCourses);

        // NEW : comparaison "réelle" : Planifium + Avis + CSV
        app.get("/courses/compare-full", courseController::compareCoursesFull);

        app.get("/courses/{id}/results", courseController::getAcademicResults);
        app.get("/courses/{id}/eligibility", courseController::getEligibility);
        app.get("/courses/{id}", courseController::getCourseById);
    }

    // -----------------------------
    // PROGRAMS
    // -----------------------------
    private static void registerProgramRoutes(Javalin app, ProgramController programController) {
        app.get("/programs/{id}", programController::getProgram);
        app.get("/programs/{id}/courses", programController::getProgramCoursesOfferedInSemester);
    }

    // -----------------------------
    // REVIEWS
    // -----------------------------
    private static void registerReviewRoutes(Javalin app, ReviewController reviewController) {
        app.get("/avis/{courseId}", reviewController::getReviews);
        app.get("/avis/{courseId}/aggregate", reviewController::getAggregate);
        app.post("/avis", reviewController::createReview);
    }

    // -----------------------------
    // SETS
    // -----------------------------
    private static void registerCourseSetRoutes(Javalin app, CourseSetController courseSetController) {
        app.post("/sets", courseSetController::createSet);
        app.get("/sets/{id}", courseSetController::getSet);
        app.get("/sets/{id}/schedule", courseSetController::getSetSchedule);
        app.get("/sets/{id}/conflicts", courseSetController::getSetConflicts); // BONUS: Détection conflits
    }
}
