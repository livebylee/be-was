package db;

import model.Article;
import model.User;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.controller.CreateUserController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private static Map<String, User> sessions = new ConcurrentHashMap<>();
    //private static Map<String, Article> articles = new ConcurrentHashMap<>();
    private static List<Article> sortedArticles = new CopyOnWriteArrayList<>();

    // --- User 관련 ---
    public static void addUser(User user) {
        String sql = "INSERT INTO users (user_id,password,name) VALUES (?, ?, ?)";

        try (Connection connec = DBConnection.getConnection();
             PreparedStatement pstmt = connec.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());

            pstmt.executeUpdate();
            logger.debug("[DB 저장 성공] user id :" + user.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static User findUserById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // 결과가 있다면
                    return new User(
                            rs.getString("user_id"),
                            rs.getString("password"),
                            rs.getString("name")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User findUserByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("user_id"),
                            rs.getString("password"),
                            rs.getString("name")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Collection<User> findAll() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("user_id"),
                        rs.getString("password"),
                        rs.getString("name")
                );
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    // --- Session 관련 ---
    public static void addSession(String sessionId, User user) {
        sessions.put(sessionId, user);
    }

    public static User getUserBySessionId(String sessionId) {
        if (sessionId == null) return null;
        return sessions.get(sessionId);
    }

    // --- Article 관련 ---
    public static void addArticle(Article article) {
        String sql = "INSERT INTO ARTICLE (ID, AUTHOR_ID, CONTENT, IMAGE_PATH) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, article.getId());
            pstmt.setString(2, article.getAuthorId());
            pstmt.setString(3, article.getContent());
            pstmt.setString(4, article.getImagePath());

            pstmt.executeUpdate();
            System.out.println(" [DB 저장 성공] 작성자: " + article.getAuthorId());

        } catch (SQLException e) {
            System.err.println(" [DB 저장 실패] error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Article findArticleById(String id) {
        String sql = "SELECT * FROM ARTICLE WHERE ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Article(
                            rs.getString("ID"),
                            rs.getString("AUTHOR_ID"),
                            rs.getString("CONTENT"),
                            rs.getString("IMAGE_PATH"),
                            rs.getTimestamp("CREATED_AT").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Article> findAllArticles() {
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT * FROM ARTICLE ORDER BY CREATED_AT DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                articles.add(new Article(
                        rs.getString("ID"),
                        rs.getString("AUTHOR_ID"),
                        rs.getString("CONTENT"),
                        rs.getString("IMAGE_PATH"),
                        rs.getTimestamp("CREATED_AT").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }
}