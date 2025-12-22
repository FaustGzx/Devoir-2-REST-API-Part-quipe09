// src/main/java/com/diro/ift2255/service/UserService.java
package com.diro.ift2255.service;

import com.diro.ift2255.model.User;
import java.util.*;

public class UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    public UserService() {
        users.put(nextId, new User(nextId++, "Alice", "alice@example.com"));
        users.put(nextId, new User(nextId++, "Bob", "bob@example.com"));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    public void createUser(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
    }

    public void updateUser(int id, User updated) {
        updated.setId(id);
        users.put(id, updated);
    }

    public void deleteUser(int id) {
        users.remove(id);
    }
}
