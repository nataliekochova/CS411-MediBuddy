package com.medibuddy.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:medibuddy.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL
                );
                """;

        String createMedicationsTable = """
                CREATE TABLE IF NOT EXISTS medications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    brand_name TEXT,
                    generic_name TEXT,
                    manufacturer TEXT,
                    purpose TEXT,
                    indications TEXT,
                    warnings TEXT,
                    label_dosage TEXT,
                    user_dose TEXT,
                    user_form TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                );
                """;

        String createSchedulesTable = """
                CREATE TABLE IF NOT EXISTS schedules (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    medication_id INTEGER NOT NULL,
                    day TEXT,
                    time TEXT,
                    FOREIGN KEY(medication_id) REFERENCES medications(id)
                );
                """;
        
        String createAdherenceTable = """
            CREATE TABLE IF NOT EXISTS adherence (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                schedule_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                taken INTEGER,
                FOREIGN KEY(schedule_id) REFERENCES schedules(id),
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.execute(createUsersTable);
            stmt.execute(createMedicationsTable);
            stmt.execute(createSchedulesTable);
            stmt.execute(createAdherenceTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}