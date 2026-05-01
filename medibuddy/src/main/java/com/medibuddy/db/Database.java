package com.medibuddy.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:medibuddy.db";
    private static final DateTimeFormatter LEGACY_TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
    private static final List<String> DAYS_OF_WEEK = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

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
                    start_date TEXT,
                    end_date TEXT,
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
        String createEmergencyContactsTable = """
            CREATE TABLE IF NOT EXISTS emergency_contacts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
            """;   
            
        String createCriticalAlertLogsTable = """
        CREATE TABLE IF NOT EXISTS critical_alert_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            schedule_id INTEGER NOT NULL,
            alert_date TEXT NOT NULL,
            sent_at TEXT NOT NULL,
            UNIQUE(user_id, schedule_id, alert_date),
            FOREIGN KEY(user_id) REFERENCES users(id),
            FOREIGN KEY(schedule_id) REFERENCES schedules(id)
        );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");
            

            stmt.execute(createUsersTable);
            stmt.execute(createMedicationsTable);
            stmt.execute(createSchedulesTable);
            stmt.execute(createAdherenceTable);
            stmt.execute(createEmergencyContactsTable);
            stmt.execute(createCriticalAlertLogsTable);
            try {
                stmt.execute("ALTER TABLE schedules ADD COLUMN critical_alert_enabled INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE schedules ADD COLUMN emergency_contact_id INTEGER");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE schedules ADD COLUMN missed_window_minutes INTEGER DEFAULT 30");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE medications ADD COLUMN start_date TEXT");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE medications ADD COLUMN end_date TEXT");
            } catch (SQLException ignored) {}

            ensureScheduleDayColumn(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ensureScheduleDayColumn(Connection conn) throws SQLException {
        if (hasColumn(conn, "schedules", "day")) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE schedules ADD COLUMN day TEXT");
        }

        migrateLegacySchedules(conn);
    }

    private static boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {

            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void migrateLegacySchedules(Connection conn) throws SQLException {
        String selectSql = """
                SELECT id, medication_id, day, time, frequency_type,
                       critical_alert_enabled, emergency_contact_id, missed_window_minutes
                FROM schedules
                ORDER BY id
                """;

        String updateSql = """
                UPDATE schedules
                SET day = ?, time = ?, critical_alert_enabled = ?, emergency_contact_id = ?, missed_window_minutes = ?
                WHERE id = ?
                """;

        String insertSql = """
                INSERT INTO schedules (
                    medication_id, day, time, critical_alert_enabled, emergency_contact_id, missed_window_minutes
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement select = conn.prepareStatement(selectSql);
             ResultSet rs = select.executeQuery();
             PreparedStatement update = conn.prepareStatement(updateSql);
             PreparedStatement insert = conn.prepareStatement(insertSql)) {

            while (rs.next()) {
                String existingDay = rs.getString("day");
                if (existingDay != null && !existingDay.isBlank()) {
                    continue;
                }

                List<String> days = inferLegacyDays(rs.getString("frequency_type"));
                if (days.isEmpty()) {
                    continue;
                }

                String normalizedTime = normalizeLegacyTime(rs.getString("time"));
                int criticalAlertEnabled = rs.getInt("critical_alert_enabled");
                Object emergencyContactId = rs.getObject("emergency_contact_id");
                int missedWindowMinutes = rs.getInt("missed_window_minutes");
                if (missedWindowMinutes <= 0) {
                    missedWindowMinutes = 30;
                }

                update.setString(1, days.get(0));
                update.setString(2, normalizedTime);
                update.setInt(3, criticalAlertEnabled);
                if (emergencyContactId == null) {
                    update.setNull(4, java.sql.Types.INTEGER);
                } else {
                    update.setObject(4, emergencyContactId);
                }
                update.setInt(5, missedWindowMinutes);
                update.setInt(6, rs.getInt("id"));
                update.executeUpdate();

                for (int i = 1; i < days.size(); i++) {
                    insert.setInt(1, rs.getInt("medication_id"));
                    insert.setString(2, days.get(i));
                    insert.setString(3, normalizedTime);
                    insert.setInt(4, criticalAlertEnabled);
                    if (emergencyContactId == null) {
                        insert.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        insert.setObject(5, emergencyContactId);
                    }
                    insert.setInt(6, missedWindowMinutes);
                    insert.executeUpdate();
                }
            }
        }
    }

    private static List<String> inferLegacyDays(String frequencyType) {
        if (frequencyType == null || frequencyType.isBlank()) {
            return new ArrayList<>();
        }

        if ("daily".equalsIgnoreCase(frequencyType.trim())) {
            return new ArrayList<>(DAYS_OF_WEEK);
        }

        return new ArrayList<>();
    }

    private static String normalizeLegacyTime(String rawTime) {
        if (rawTime == null || rawTime.isBlank()) {
            return "08:00 AM";
        }

        String trimmed = rawTime.trim().toUpperCase(Locale.US);

        try {
            return LocalTime.parse(trimmed, DISPLAY_TIME_FORMAT).format(DISPLAY_TIME_FORMAT);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalTime.parse(trimmed, LEGACY_TIME_FORMAT).format(DISPLAY_TIME_FORMAT);
        } catch (DateTimeParseException ignored) {
            return rawTime.trim();
        }
    }
}
