package com.scc.dao;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void init() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING'
            );
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            if (conn == null) {
                System.err.println("Database connection failed!");
                return;
            }

            stmt.execute(createUsersTable);

            // ✅ Matches screenshot: ROLE: ADMIN
            String insertAdmin = """
                INSERT OR IGNORE INTO users (username, password, roles, status)
                VALUES ('admin', 'admin123', 'ADMIN', 'APPROVED');
                """;
            stmt.execute(insertAdmin);

        } catch (Exception e) {
            System.err.println("Init Error: " + e.getMessage());
        }
    }
}