package com.diro.ift2255.config;

import com.diro.ift2255.controller.CourseController;
import com.diro.ift2255.controller.UserController;
import com.diro.ift2255.service.UserService;
import com.diro.ift2255.service.CourseService;
import com.diro.ift2255.util.HttpClientApi;

import io.javalin.Javalin;

public class Routes {

    public static void register(Javalin app) {
        registerUserRoutes(app);
        registerCourseRoutes(app);
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

        // CU1 : Recherche de cours
        app.get("/courses", courseController::getAllCourses);

        // CU3 : Comparer des cours (doit rester avant /courses/{id})
        app.get("/courses/comparer", courseController::compareCourses);

        // Vérifier l'éligibilité à un cours (basé sur les prérequis + cours déjà faits)
        // Exemple :
        //   GET /courses/IFT2255/eligibility?completed=IFT1015,IFT1025
        app.get("/courses/{id}/eligibility", courseController::getEligibility);

        // CU2 : Détails d'un cours
        app.get("/courses/{id}", courseController::getCourseById);
    }
}
