package edu.univ.erp.data;

import edu.univ.erp.domain.UserAuth;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthDao {
    public UserAuth getUserByUsername(String username) {
        String sql = "SELECT user_id, username, pass_hash, role, status FROM users WHERE username = ?";
        try (Connection conn = DbPool.getAuthDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserAuth user = new UserAuth();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                String hashFromDb = rs.getString("pass_hash");
                user.setPasswordHash(hashFromDb != null ? hashFromDb.trim() : null);
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }
    public UserAuth createUser(String username, String passHash, String role) {
        String sql = "INSERT INTO users (username, pass_hash, role, status) VALUES (?, ?, ?, 'ACTIVE')";
        UserAuth newUser = null;
        try (Connection conn = DbPool.getAuthDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passHash);
            ps.setString(3, role);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    newUser = new UserAuth(userId, username, role, "ACTIVE");
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("User creation failed due to unique constraint: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.err.println("Error creating new user: " + e.getMessage());
            e.printStackTrace();
        }
        return newUser;
    }
    public List<UserAuth> getAllUsers() {
        List<UserAuth> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status FROM users ORDER BY user_id";
        try (Connection conn = DbPool.getAuthDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UserAuth user = new UserAuth();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
    public boolean updatePassword(String username, String newHash) {
        String sql = "UPDATE users SET pass_hash = ? WHERE username = ?";
        try (Connection conn = DbPool.getAuthDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
