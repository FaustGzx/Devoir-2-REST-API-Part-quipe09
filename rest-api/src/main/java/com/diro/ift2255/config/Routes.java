package com.diro.ift2255.config;

import com.diro.ift2255.controller.*;
import com.diro.ift2255.service.*;
import com.diro.ift2255.util.HttpClientApi;
import io.javalin.Javalin;

public class Routes {

    public static void register(Javalin app) {
        registerUserRoutes(app);
        registerCourseRoutes(app);
        registerProgramRoutes(app);
        registerReviewRoutes(app);
        registerCourseSetRoutes(app);
    }

    private static void registerUserRoutes(Javalin app) {
        UserService userService = new UserService();
        UserController userController = new UserController(userService);

        app.get("/users", userController::getAllUsers);
        app.get("/users/{id}", userController::getUserById);
        app.post("/users", userController::createUser);
        app.put("/users/{id}", userController::updateUser);
        app.delete("/users/{id}", userController::deleteUser);
    }

    private static void registerCourseRoutes(Javalin app) {
        CourseService courseService = new CourseService(new HttpClientApi());
        CourseController courseController = new CourseController(courseService);

        app.get("/courses", courseController::getAllCourses);
        app.get("/courses/comparer", courseController::compareCourses);
        app.get("/courses/{id}/results", courseController::getAcademicResults);
        app.get("/courses/{id}/eligibility", courseController::getEligibility);
        app.get("/courses/{id}", courseController::getCourseById);
    }

    private static void registerProgramRoutes(Javalin app) {
        HttpClientApi api = new HttpClientApi();
        CourseService courseService = new CourseService(api);
        ProgramService programService = new ProgramService(api, courseService);
        ProgramController programController = new ProgramController(programService);

        app.get("/programs/{id}", programController::getProgram);
        app.get("/programs/{id}/courses", programController::getProgramCoursesOfferedInSemester);
    }

    private static void registerReviewRoutes(Javalin app) {
        // Stockage local: crée un dossier "data" à la racine du projet
        ReviewService reviewService = new ReviewService("data/reviews.json");
        ReviewController reviewController = new ReviewController(reviewService);

        app.get("/avis/{courseId}", reviewController::getReviews);
        app.post("/avis", reviewController::createReview);
    }

    private static void registerCourseSetRoutes(Javalin app) {
        CourseService courseService = new CourseService(new HttpClientApi());
        CourseSetService setService = new CourseSetService(courseService);
        CourseSetController setController = new CourseSetController(setService);

        app.post("/sets", setController::createSet);
        app.get("/sets/{id}", setController::getSet);
        app.get("/sets/{id}/schedule", setController::getSetSchedule);
    }
}
