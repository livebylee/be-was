package db;

import model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, User> sessions = new HashMap<>();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static User findUserByName(String name) {
        return users.values().stream()
                .filter(user -> user.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static Collection<User> findAll() {
        return users.values();
    }

    public static void addSession(String sessionId, User user) {
        sessions.put(sessionId, user);
    }

    public static User getUserBySessionId(String sessionId) {
        return sessions.get(sessionId);
    }
}
