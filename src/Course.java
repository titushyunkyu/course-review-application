package edu.virginia.sde.reviews;

public class Course {
    private int id;
    private String subject;
    private int number;
    private String title;
    private Double averageRating;

    public Course(String subject, int number, String title) {
        this.subject = subject;
        this.number = number;
        this.title = title;
        this.averageRating = null;
    }

    public Course(int id, String subject, int number, String title, Double averageRating) {
        this.id = id;
        this.subject = subject;
        this.number = number;
        this.title = title;
        this.averageRating = averageRating;
    }
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getSubject() {return subject;}
    public void setSubject(String subject) {this.subject = subject;}
    public int getNumber() {return number;}
    public void setNumber(int number) {this.number = number;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public Double getAverageRating() {return averageRating;}
    public void setAverageRating(Double averageRating) {this.averageRating = averageRating;}

    public String getFormattedRating() {
        if (averageRating == null) {
            return "";
        }
        return String.format("%.2f", averageRating);
    }

    public String getFullCourseName() {
        return subject + " " + number + ": " + title;
    }

    @Override
    public String toString() {
        return getFullCourseName();
    }
}
