package edu.virginia.sde.reviews;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Note: Structure inspired by my DatabaseDriver/BusLineService from HW5 in this course (see my HW5 git repo).

public class DatabaseDriver {

    private static final String DB_URL = "jdbc:sqlite:coursereviews.sqlite";
    private static Connection connection;

    public static void initialize(){
        try {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement s = connection.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
            }
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);

            try (Statement s = connection.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public static void closeConnection(){
        if (connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createTables() throws SQLException{
        try (Statement statement = getConnection().createStatement()){
            // USERS table
            statement.executeUpdate("""
                   CREATE TABLE IF NOT EXISTS USERS (
                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                       username TEXT NOT NULL UNIQUE,
                       password TEXT NOT NULL
                   );
                """);

            // COURSES table
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS COURSES (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        subject TEXT NOT NULL,
                        CRN INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        UNIQUE(subject, CRN, title)
                   );
                """);

            // REVIEWS table
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS REVIEWS (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        course_id INTEGER NOT NULL,
                        rating INTEGER NOT NULL,
                        comment TEXT,
                        timestamp TEXT NOT NULL,
                        UNIQUE (user_id, course_id),
                        FOREIGN KEY (user_id) REFERENCES USERS(id),
                        FOREIGN KEY (course_id) REFERENCES COURSES(id)
                   );
                """);
        }
    }

}
