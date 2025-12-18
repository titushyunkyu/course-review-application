package edu.virginia.sde.reviews;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDatabaseManager {
    private Connection connection;

    public CourseDatabaseManager(Connection connection) {
        this.connection = connection;
    }

    //Get all courses from the database with their average ratings
    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = """
            SELECT c.id, c.subject, c.CRN, c.title, AVG(r.rating) as avg_rating
            FROM COURSES c
            LEFT JOIN REVIEWS r ON c.id = r.course_id
            GROUP BY c.id, c.subject, c.CRN, c.title
            ORDER BY c.subject, c.CRN
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String subject = rs.getString("subject");
                int crn = rs.getInt("CRN");
                String title = rs.getString("title");

                //Handle NULL average rating properly
                double avgRatingValue = rs.getDouble("avg_rating");
                Double avgRating = rs.wasNull() ? null : avgRatingValue;

                courses.add(new Course(id, subject, crn, title, avgRating));
            }
        }
        return courses;
    }

    //Search courses by subject, CRN, and/or title
    public List<Course> searchCourses(String subject, String crn, String title) throws SQLException {
        List<Course> courses = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT c.id, c.subject, c.CRN, c.title, AVG(r.rating) as avg_rating
            FROM COURSES c
            LEFT JOIN REVIEWS r ON c.id = r.course_id
            WHERE 1=1
            """);

        if (subject != null && !subject.trim().isEmpty()) {
            sql.append(" AND UPPER(c.subject) = UPPER(?)");
        }
        if (crn != null && !crn.trim().isEmpty()) {
            sql.append(" AND c.CRN = ?");
        }
        if (title != null && !title.trim().isEmpty()) {
            sql.append(" AND UPPER(c.title) LIKE UPPER(?)");
        }

        sql.append(" GROUP BY c.id, c.subject, c.CRN, c.title ORDER BY c.subject, c.CRN");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (subject != null && !subject.trim().isEmpty()) {
                pstmt.setString(paramIndex++, subject.trim());
            }
            if (crn != null && !crn.trim().isEmpty()) {
                try {
                    pstmt.setInt(paramIndex++, Integer.parseInt(crn.trim()));
                } catch (NumberFormatException e) {
                    return List.of(); // invalid number → no results
                }
            }
            if (title != null && !title.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + title.trim() + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String subj = rs.getString("subject");
                    int courseNum = rs.getInt("CRN");
                    String ttl = rs.getString("title");

                    // Handle NULL average rating properly
                    double avgRatingValue = rs.getDouble("avg_rating");
                    Double avgRating = rs.wasNull() ? null : avgRatingValue;

                    courses.add(new Course(id, subj, courseNum, ttl, avgRating));
                }
            }
        }
        return courses;
    }

    //Add a new course to database
    public boolean addCourse(Course course) throws SQLException {
        //First check if course already exists
        String checkSql = "SELECT id FROM COURSES WHERE UPPER(subject) = UPPER(?) AND CRN = ? AND UPPER(title) = UPPER(?)";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, course.getSubject());
            checkStmt.setInt(2, course.getNumber());
            checkStmt.setString(3, course.getTitle());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return false;
                }
            }
        }

        //Insert new course
        String insertSql = "INSERT INTO COURSES (subject, CRN, title) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            pstmt.setString(1, course.getSubject().toUpperCase());
            pstmt.setInt(2, course.getNumber());
            pstmt.setString(3, course.getTitle());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                //Get last inserted ID
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        course.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    //Get specific course by ID
    public Course getCourseById(int courseId) throws SQLException {
        String sql = """
            SELECT c.id, c.subject, c.CRN, c.title, AVG(r.rating) as avg_rating
            FROM COURSES c
            LEFT JOIN REVIEWS r ON c.id = r.course_id
            WHERE c.id = ?
            GROUP BY c.id, c.subject, c.CRN, c.title
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String subject = rs.getString("subject");
                    int crn = rs.getInt("CRN");
                    String title = rs.getString("title");

                    double avgRatingValue = rs.getDouble("avg_rating");
                    Double avgRating = rs.wasNull() ? null : avgRatingValue;

                    return new Course(courseId, subject, crn, title, avgRating);
                }
            }
        }
        return null;
    }
}