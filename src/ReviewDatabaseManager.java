package edu.virginia.sde.reviews;
import java.sql.*;
import java.util.*;

public class ReviewDatabaseManager {
    private final Connection connection;

    public ReviewDatabaseManager(Connection connection) {
        this.connection = connection;
    }

    public List<Review> getReviewsForCourse(int courseId) throws SQLException {
        String sql = """
            SELECT id, user_id, course_id, rating, comment, timestamp
            FROM REVIEWS
            WHERE course_id = ?
            ORDER BY timestamp DESC
        """;

        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("course_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getString("timestamp")
                    ));
                }
            }
        }
        return reviews;
    }
    public List<Review> getReviewsForUser(int userId) throws SQLException {
        String sql = """
        SELECT id, user_id, course_id, rating, comment, timestamp
        FROM REVIEWS
        WHERE user_id = ?
        ORDER BY timestamp DESC
    """;

        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("course_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getString("timestamp")
                    ));
                }
            }
        }
        return reviews;
    }

    public Optional<Review> getUserReview(int userId, int courseId) throws SQLException {
        String sql = """
            SELECT id, user_id, course_id, rating, comment, timestamp
            FROM REVIEWS
            WHERE user_id = ? AND course_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Review(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("course_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getString("timestamp")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public void addReview(Review review) throws SQLException {
        String sql = """
            INSERT INTO REVIEWS (user_id, course_id, rating, comment, timestamp)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, review.getUserId());
            ps.setInt(2, review.getCourseId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            ps.setString(5, review.getTimestamp());
            ps.executeUpdate();
        }
    }

    public void updateReview(int userId, int courseId, int rating, String comment, String timestamp)
            throws SQLException {
        String sql = """
            UPDATE REVIEWS
            SET rating = ?, comment = ?, timestamp = ?
            WHERE user_id = ? AND course_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setString(3, timestamp);
            ps.setInt(4, userId);
            ps.setInt(5, courseId);
            ps.executeUpdate();
        }
    }

    public void deleteReview(int userId, int courseId) throws SQLException {
        String sql = "DELETE FROM REVIEWS WHERE user_id = ? AND course_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }
}
