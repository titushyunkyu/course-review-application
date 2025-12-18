package edu.virginia.sde.reviews;

public class Review {
    private int id;
    private int userId;
    private int courseId;
    private int rating;
    private String comment;
    private String timestamp;

    public Review(int id, int userId, int courseId, int rating, String comment, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public Review(int userId, int courseId, int rating, String comment, String timestamp) {
        this(0, userId, courseId, rating, comment, timestamp);
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getCourseId() { return courseId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getTimestamp() { return timestamp; }
}
