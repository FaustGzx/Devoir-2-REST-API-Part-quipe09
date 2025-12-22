// src/main/java/com/diro/ift2255/controller/UserController.java
package com.diro.ift2255.controller;

import java.util.List;
import java.util.Optional;

import com.diro.ift2255.model.User;
import com.diro.ift2255.service.UserService;
import com.diro.ift2255.util.ResponseUtil;
import com.diro.ift2255.util.ValidationUtil;

import io.javalin.http.Context;

public class UserController {

    private final UserService service;

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

    public void getAllUsers(Context ctx) {
        List<User> users = service.getAllUsers();
        ctx.json(ResponseUtil.ok(users));
    }

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

    public void createUser(Context ctx) {
        User user = ctx.bodyAsClass(User.class);
        if (user == null || user.getEmail() == null || !ValidationUtil.isEmail(user.getEmail())) {
            ctx.status(400).json(ResponseUtil.error("Format d'email invalide."));
            return;
        }
        service.createUser(user);
        ctx.status(201).json(ResponseUtil.ok(user));
    }

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

    public void deleteUser(Context ctx) {
        Integer id = parseIdOrBadRequest(ctx);
        if (id == null) return;

        service.deleteUser(id);
        ctx.status(204);
    }
}
