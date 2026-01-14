package db;

import model.Article;
import model.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Database {
    private static Map<String, User> users = new ConcurrentHashMap<>();
    private static Map<String, User> sessions = new ConcurrentHashMap<>();
    private static Map<String, Article> articles = new ConcurrentHashMap<>();
    private static List<Article> sortedArticles = new CopyOnWriteArrayList<>();

    // --- User 관련 ---
    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static User findUserByName(String name) {
        return users.get(name);
    }

    public static Collection<User> findAll() {
        return users.values();
    }

    // --- Session 관련 ---
    public static void addSession(String sessionId, User user) {
        sessions.put(sessionId, user);
    }

    public static User getUserBySessionId(String sessionId) {
        return sessions.get(sessionId);
    }

    // --- Article 관련 ---
    public static void addArticle(Article article) {
        articles.put(article.getId(), article);
        sortedArticles.add(article);
        sortedArticles.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    }

    public static Article findArticleById(String id) {
        return articles.get(id);
    }

    public static List<Article> findAllArticles() {
        return Collections.unmodifiableList(sortedArticles);
    }
}