// src/main/java/com/diro/ift2255/model/User.java
package com.diro.ift2255.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Représente un utilisateur de l’application.
 *
 * <p>Un {@code User} peut contenir des informations de base
 * permettant une personnalisation éventuelle de l’expérience
 * (profil étudiant, préférences, etc.).</p>
 */
public class User {
    private int id;
    private String name;
    private String email;
     /**
     * Construit un {@code User} vide.
     *
     * <p>Utilisé pour la sérialisation et la désérialisation JSON.</p>
     */
    public User() {}
     /**
     * Construit un utilisateur avec ses informations principales.
     *
     * @param id identifiant unique de l’utilisateur
     * @param name nom de l’utilisateur
     * @param email adresse courriel de l’utilisateur
     */
    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
