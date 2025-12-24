// src/main/java/com/diro/ift2255/controller/UserController.java
package com.diro.ift2255.controller;

import java.util.List;
import java.util.Optional;

import com.diro.ift2255.model.User;
import com.diro.ift2255.service.UserService;
import com.diro.ift2255.util.ResponseUtil;
import com.diro.ift2255.util.ValidationUtil;

import io.javalin.http.Context;
/**
 * Contrôleur REST (Javalin) responsable des opérations liées aux utilisateurs.
 *
 * <p>Expose des endpoints pour :</p>
 * <ul>
 *   <li>consulter la liste des utilisateurs</li>
 *   <li>consulter un utilisateur par identifiant</li>
 *   <li>créer, modifier et supprimer un utilisateur</li>
 * </ul>
 *
 * Les réponses JSON sont standardisées via {@link com.diro.ift2255.util.ResponseUtil}.
 *
 * <p>Note : La gestion de compte utilisateur n'est pas obligatoire dans l'énoncé,
 * mais peut être incluse pour améliorer l'expérience.</p>
 */
public class UserController {
    /** Service applicatif responsable de la logique métier et de la persistance des utilisateurs. */
    private final UserService service;
    /**
     * Construit un {@code UserController}.
     *
    * @param service service de gestion des utilisateurs
    */
    public UserController(UserService service) {
        this.service = service;
    }

    private Integer parseIdOrBadRequest(Context ctx) {
        try {
            return Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json(ResponseUtil.error("Paramètre id invalide (doit être un entier)."));
            return null;
        }
    }
    /**
    * Retourne la liste de tous les utilisateurs.
    *
    * <p>Endpoint : {@code GET /users}</p>
    *
    * @param ctx contexte Javalin (réponse JSON)
    */
    public void getAllUsers(Context ctx) {
        List<User> users = service.getAllUsers();
        ctx.json(ResponseUtil.ok(users));
    }
    /**
    * Retourne un utilisateur à partir de son identifiant.
    *
    * <p>Endpoint : {@code GET /users/{id}}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code id} + réponse JSON)
    */
    public void getUserById(Context ctx) {
        Integer id = parseIdOrBadRequest(ctx);
        if (id == null) return;

        Optional<User> user = service.getUserById(id);
        if (user.isPresent()) {
            ctx.json(ResponseUtil.ok(user.get()));
        } else {
            ctx.status(404).json(ResponseUtil.error("Aucun utilisateur ne correspond à l'ID: " + id));
        }
    }
    /**
    * Crée un nouvel utilisateur.
    *
    * <p>Endpoint : {@code POST /users}</p>
    *
    * @param ctx contexte Javalin (corps JSON + réponse JSON)
    */
    public void createUser(Context ctx) {
        User user = ctx.bodyAsClass(User.class);
        if (user == null || user.getEmail() == null || !ValidationUtil.isEmail(user.getEmail())) {
            ctx.status(400).json(ResponseUtil.error("Format d'email invalide."));
            return;
        }
        service.createUser(user);
        ctx.status(201).json(ResponseUtil.ok(user));
    }
    /**
    * Met à jour un utilisateur existant.
    *
    * <p>Endpoint : {@code PUT /users/{id}}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code id} + corps JSON + réponse JSON)
    */
    public void updateUser(Context ctx) {
        Integer id = parseIdOrBadRequest(ctx);
        if (id == null) return;

        User updated = ctx.bodyAsClass(User.class);
        if (updated == null) {
            ctx.status(400).json(ResponseUtil.error("Body invalide."));
            return;
        }
        if (updated.getEmail() != null && !ValidationUtil.isEmail(updated.getEmail())) {
            ctx.status(400).json(ResponseUtil.error("Format d'email invalide."));
            return;
        }

        service.updateUser(id, updated);
        ctx.json(ResponseUtil.ok(updated));
    }
    /**
    * Supprime un utilisateur à partir de son identifiant.
    *
    * <p>Endpoint : {@code DELETE /users/{id}}</p>
    *
    * @param ctx contexte Javalin (paramètre de chemin {@code id} + réponse JSON)
    */
    public void deleteUser(Context ctx) {
        Integer id = parseIdOrBadRequest(ctx);
        if (id == null) return;

        service.deleteUser(id);
        ctx.status(204);
    }
}
