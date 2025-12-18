package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyReviewsScene {

    private final Stage primaryStage;
    private final int userId;

    private Connection connection;
    private ReviewDatabaseManager reviewDb;
    private CourseDatabaseManager courseDb;

    private TableView<MyReviewRow> table;
    private Label statusLabel;
    private Label reviewCountLabel;

    public MyReviewsScene(Stage primaryStage, int userId) {
        this.primaryStage = primaryStage;
        this.userId = userId;

        try {
            this.connection = DatabaseDriver.getConnection();
            this.reviewDb = new ReviewDatabaseManager(connection);
            this.courseDb = new CourseDatabaseManager(connection);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to connect to database: " + e.getMessage());
        }
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: white;");

        root.setTop(buildTopBar());
        root.setCenter(buildCenter());
        root.setBottom(buildBottom());

        refresh();

        return new Scene(root, 1050, 650);
    }

    private Node buildTopBar() {
        Label title = new Label("My Reviews");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button back = new Button("Back");
        back.setOnAction(e -> goBackToCourseSearch());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(12, back, spacer, title);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 12, 0));
        return top;
    }

    private Node buildCenter() {

        reviewCountLabel = new Label("You have 0 reviews.");
        reviewCountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        reviewCountLabel.setAlignment(Pos.CENTER);

        HBox countBox = new HBox(reviewCountLabel);
        countBox.setAlignment(Pos.CENTER);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No reviews yet. Write a review from a course page!"));

        TableColumn<MyReviewRow, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseDisplay"));

        TableColumn<MyReviewRow, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setMaxWidth(120);

        TableColumn<MyReviewRow, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timeCol.setMaxWidth(220);

        TableColumn<MyReviewRow, String> commentCol = new TableColumn<>("Comment");
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));

        table.getColumns().addAll(courseCol, ratingCol, timeCol, commentCol);

        // double click row to open that course’s reviews page
        table.setRowFactory(tv -> {
            TableRow<MyReviewRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openSelectedCourse();
                }
            });
            return row;
        });

        VBox center = new VBox(10, countBox, table);
        center.setPadding(new Insets(10, 0, 10, 0));
        return center;
    }

    private Node buildBottom() {
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #b00020;");

        Button open = new Button("Open Course");
        open.setOnAction(e -> openSelectedCourse());

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> refresh());

        HBox buttons = new HBox(10, open, refresh);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        BorderPane bottom = new BorderPane();
        bottom.setLeft(statusLabel);
        bottom.setRight(buttons);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        return bottom;
    }

    private void refresh() {
        statusLabel.setText("");

        if (reviewDb == null || courseDb == null) {
            statusLabel.setText("Database not available.");
            return;
        }

        try {
            List<Review> myReviews = reviewDb.getReviewsForUser(userId);

            if (reviewCountLabel != null){
                int n = myReviews.size();
                reviewCountLabel.setText("You have " + n + " review" + (n == 1 ? "" : "s") + ".");
            }

            List<MyReviewRow> built = new ArrayList<>();
            for (Review r : myReviews) {
                Course c = courseDb.getCourseById(r.getCourseId());
                if (c != null) {
                    built.add(new MyReviewRow(
                            c.getId(),
                            c.getFullCourseName(),
                            r.getRating(),
                            safe(r.getTimestamp()),
                            safe(r.getComment())
                    ));
                }
            }

            ObservableList<MyReviewRow> rows = FXCollections.observableArrayList(built);
            table.setItems(rows);

        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Database error: " + ex.getMessage());
        }
    }

    private void openSelectedCourse() {
        MyReviewRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a review first.");
            return;
        }

        try {
            Course course = courseDb.getCourseById(selected.getCourseId());
            if (course == null) {
                statusLabel.setText("Could not find that course in the database.");
                return;
            }

            CourseReviewsScene scene = new CourseReviewsScene(primaryStage, userId, course);
            primaryStage.setScene(scene.createScene());
            primaryStage.setTitle("Course Reviews");

        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Database error: " + ex.getMessage());
        }
    }

    private void goBackToCourseSearch() {
        CourseSearchScene search = new CourseSearchScene(primaryStage, userId);
        primaryStage.setScene(search.createScene());
        primaryStage.setTitle("Course Reviews");
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Small row model for the table
    public static class MyReviewRow {
        private final int courseId;
        private final String courseDisplay;
        private final int rating;
        private final String timestamp;
        private final String comment;

        public MyReviewRow(int courseId, String courseDisplay, int rating, String timestamp, String comment) {
            this.courseId = courseId;
            this.courseDisplay = courseDisplay;
            this.rating = rating;
            this.timestamp = timestamp;
            this.comment = comment;
        }

        public int getCourseId() { return courseId; }
        public String getCourseDisplay() { return courseDisplay; }
        public int getRating() { return rating; }
        public String getTimestamp() { return timestamp; }
        public String getComment() { return comment; }
    }
}
