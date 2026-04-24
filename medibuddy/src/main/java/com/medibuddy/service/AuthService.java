package com.medibuddy.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.medibuddy.db.Database;

import java.sql.*;

public class AuthService {

    public boolean createAccount(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        String passwordHash = BCrypt.withDefaults()
                .hashToString(12, password.toCharArray());

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public Integer login(String username, String password) {
        String sql = "SELECT id, password_hash FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }

            int userId = rs.getInt("id");
            String storedHash = rs.getString("password_hash");

            BCrypt.Result result = BCrypt.verifyer()
                    .verify(password.toCharArray(), storedHash);

            if (result.verified) {
                return userId;
            }

            return null;

        } catch (SQLException e) {
            return null;
        }
    }
}