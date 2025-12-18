package edu.virginia.sde.reviews;

import java.sql.*;
import java.util.Optional;

public class UserManagement {

    // Creates a new user
    public Optional<User> createUser(String username, String password) {
        String insertQuery = "INSERT INTO USERS (username, password) VALUES (?, ?)";
        String selectQuery = "SELECT id, username, password FROM USERS WHERE username = ?";

        try {
            Connection connection = DatabaseDriver.getConnection();

            try (PreparedStatement insert = connection.prepareStatement(insertQuery)) {
                insert.setString(1, username);
                insert.setString(2, password);
                insert.executeUpdate();
            }

            try (PreparedStatement select = connection.prepareStatement(selectQuery)) {
                select.setString(1, username);

                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String uname = rs.getString("username");
                        String pwd = rs.getString("password");
                        return Optional.of(new User(id, uname, pwd));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            // If the username already exists, UNIQUE(username) will be violated on INSERT
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                return Optional.empty();
            }
            throw new RuntimeException("Unexpected error while creating user", e);
        }
    }


    // Check if username exists
    public Optional<User> findUsingUsername(String username){
        String query = "SELECT id, username, password FROM USERS WHERE username = ?";

        try {
            Connection connection = DatabaseDriver.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)){
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()){
                    if (rs.next()){
                        int id = rs.getInt("id");
                        String uname = rs.getString("username");
                        String pwd = rs.getString("password");
                        return Optional.of(new User(id, uname,pwd));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user",e);
        }
    }

    public Optional<User> findUsingUsernameAndPassword(String username, String password){
        String query =  "SELECT id, username, password FROM USERS WHERE username = ? AND password = ?";

        try {
            Connection connection = DatabaseDriver.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)){
                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()){
                    if (rs.next()){
                        int id = rs.getInt("id");
                        String uname = rs.getString("username");
                        String pwd = rs.getString("password");
                        return Optional.of(new User(id, uname,pwd));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user by username and password",e);
        }
    }

}
