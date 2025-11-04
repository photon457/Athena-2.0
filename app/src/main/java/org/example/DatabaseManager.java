package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:athena.db";
    private static final String TABLE_NAME = "chat_history";

    private Connection connection;

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTableIfNotExists();
        } catch (SQLException e) {
            System.err.println("Failed to connect to SQLite database.");
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() {
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_message TEXT NOT NULL," +
                    "ai_response TEXT NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveChatPair(String userMessage, String aiResponse) {
        String sql = "INSERT INTO " + TABLE_NAME + " (user_message, ai_response) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userMessage);
            pstmt.setString(2, aiResponse);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAllChatHistory() {
        List<Map<String, Object>> chatHistory = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY timestamp ASC";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> chatPair = new HashMap<>();
                chatPair.put("id", rs.getInt("id"));
                chatPair.put("user_message", rs.getString("user_message"));
                chatPair.put("ai_response", rs.getString("ai_response"));
                chatPair.put("timestamp", rs.getString("timestamp"));
                chatHistory.add(chatPair);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chatHistory;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}