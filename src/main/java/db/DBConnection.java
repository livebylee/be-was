package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:h2:tcp://localhost/~/test";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB 연결 실패");
        }
    }

}
