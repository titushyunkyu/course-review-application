package edu.virginia.sde.reviews;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class CourseReviewsScene {

    private final Stage primaryStage;
    private final int userId;
    private final Course course;

    private final Connection connection;
    private final CourseDatabaseManager courseDb;
    private final ReviewDatabaseManager reviewDb;

    private Label avgRatingLabel;

    private VBox reviewCardsBox;

    private ComboBox<Integer> ratingBox;
    private TextArea commentArea;
    private Button submitButton;
    private Button deleteButton;
    private Label editorMessage;

    private Optional<Review> myReview = Optional.empty();

    public CourseReviewsScene(Stage primaryStage, int userId, Course course) throws SQLException {
        this.primaryStage = primaryStage;
        this.userId = userId;
        this.course = course;

        this.connection = DatabaseDriver.getConnection();
        this.courseDb = new CourseDatabaseManager(connection);
        this.reviewDb = new ReviewDatabaseManager(connection);
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        root.setTop(buildTopBar());

        ScrollPane reviewPane = buildReviewList();
        root.setCenter(reviewPane);

        VBox editor = buildEditor();
        root.setRight(editor);

        refresh();

        return new Scene(root, 1050, 650);
    }

    private VBox buildTopBar() {
        Label title = new Label(course.getFullCourseName());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        avgRatingLabel = new Label();
        avgRatingLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: #e3f2fd;" +
                        "-fx-border-color: #2196f3;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 10;"
        );


        Button back = new Button("Back");
        back.setOnAction(e -> goBackToCourseSearch());

        HBox row = new HBox(10, back, title);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox top = new VBox(6, row, avgRatingLabel);
        top.setPadding(new Insets(0, 0, 10, 0));
        return top;
    }

    private void goBackToCourseSearch() {
        CourseSearchScene search = new CourseSearchScene(primaryStage, userId);
        primaryStage.setScene(search.createScene());
        primaryStage.setTitle("Course Reviews");
    }

    private ScrollPane buildReviewList() {
        reviewCardsBox = new VBox(10);
        reviewCardsBox.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(reviewCardsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private void rebuildReviewCards(List<Review> reviews) {
        reviewCardsBox.getChildren().clear();

        if (reviews == null || reviews.isEmpty()) {
            Label none = new Label("No reviews yet.");
            none.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
            reviewCardsBox.getChildren().add(none);
            return;
        }

        for (Review r : reviews) {
            boolean mine = (r.getUserId() == userId);

            Label rating = new Label("Rating: " + r.getRating() + "/5" + (mine ? "  (You)" : ""));
            rating.setStyle("-fx-font-weight: bold;");

            VBox card = getvBox(r, rating, mine);

            reviewCardsBox.getChildren().add(card);
        }
    }

    private static VBox getvBox(Review r, Label rating, boolean mine) {
        String ts = r.getTimestamp();
        String trimmedTs = (ts != null && ts.length() >= 16) ? ts.substring(0, 16) : ts;

        Label time = new Label(trimmedTs);
        time.setStyle("-fx-text-fill: #555;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, rating, spacer, time);
        topRow.setAlignment(Pos.CENTER_LEFT);


        String commentText = (r.getComment() == null || r.getComment().isBlank())
                ? "(No comment)"
                : r.getComment();

        Label comment = new Label(commentText);
        comment.setWrapText(true);

        VBox card = new VBox(6, topRow, comment);
        card.setPadding(new Insets(10));
        card.setMaxWidth(Double.MAX_VALUE);

        if (mine) {
            card.setStyle("-fx-background-color: #d9fdd3; -fx-background-radius: 8;");
        } else {
            card.setStyle("-fx-background-color: #f2f2f2; -fx-background-radius: 8;");
        }
        return card;
    }

    private VBox buildEditor() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setPrefWidth(350);
        box.setMinWidth(350);
        box.setMaxWidth(350);
        box.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 8;");

        Label header = new Label("Your Review");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        editorMessage = new Label();
        editorMessage.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        editorMessage.setVisible(false);

        Label ratingLabel = new Label("Rating");
        ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingBox.setPromptText("Select 1–5");

        Label commentLabel = new Label("Comment");
        commentArea = new TextArea();
        commentArea.setPromptText("Optional comment");
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(7);

        submitButton = new Button("Submit");
        submitButton.setPrefWidth(Double.MAX_VALUE);

        deleteButton = new Button("Delete");
        deleteButton.setPrefWidth(Double.MAX_VALUE);
        deleteButton.setStyle("-fx-background-color: rgba(215,54,33,0.98); -fx-text-fill: white;");
        deleteButton.setDisable(true);

        ratingBox.setOnAction(ev -> clearEditorMessage());
        commentArea.textProperty().addListener((obs, oldV, newV) -> clearEditorMessage());

        submitButton.setOnAction(e -> handleSubmit());
        deleteButton.setOnAction(e -> handleDelete());

        VBox.setVgrow(commentArea, Priority.ALWAYS);

        box.getChildren().addAll(
                header,
                editorMessage,
                ratingLabel,
                ratingBox,
                commentLabel,
                commentArea,
                submitButton,
                deleteButton
        );

        return box;
    }

    private void handleSubmit() {
        clearEditorMessage();

        Integer selected = ratingBox.getValue();
        if (selected == null) {
            showEditorError("You must select a rating (1–5) before submitting.");
            return;
        }

        try {
            int rating = selected;
            String comment = commentArea.getText();
            String ts = new Timestamp(System.currentTimeMillis()).toString();

            boolean wasUpdate = myReview.isPresent();

            if (wasUpdate) {
                reviewDb.updateReview(userId, course.getId(), rating, comment, ts);
            } else {
                reviewDb.addReview(new Review(userId, course.getId(), rating, comment, ts));
            }

            refresh();

            if (wasUpdate) {
                showEditorSuccess("Updated!");
            } else {
                showEditorSuccess("Saved!");
            }

        } catch (Exception ex) {
            showEditorError("Failed to save review: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        clearEditorMessage();

        if (myReview.isEmpty()) {
            showEditorError("No review to delete.");
            return;
        }

        try {
            reviewDb.deleteReview(userId, course.getId());
            refresh();
            showEditorSuccess("Deleted!");
        } catch (Exception ex) {
            showEditorError("Failed to delete review: " + ex.getMessage());
        }
    }

    private void syncEditorStateToMyReview() {
        if (myReview.isPresent()) {
            Review r = myReview.get();

            ratingBox.setValue(r.getRating());
            commentArea.setText(r.getComment() == null ? "" : r.getComment());

            submitButton.setText("Update");
            deleteButton.setDisable(false);
        } else {
            ratingBox.setValue(null);
            commentArea.clear();

            submitButton.setText("Submit");
            deleteButton.setDisable(true);
        }
    }

    private void showEditorError(String text) {
        editorMessage.setStyle("-fx-text-fill: rgba(206,37,37,0.9); -fx-font-weight: bold;");
        editorMessage.setText(text);
        editorMessage.setVisible(true);
    }

    private void showEditorSuccess(String text) {
        editorMessage.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        editorMessage.setText(text);
        editorMessage.setVisible(true);
    }

    private void clearEditorMessage() {
        if (editorMessage != null) {
            editorMessage.setText("");
            editorMessage.setVisible(false);
        }
    }

    private void refresh() {
        try {
            Course refreshed = courseDb.getCourseById(course.getId());
            if (refreshed == null || refreshed.getAverageRating() == null) {
                avgRatingLabel.setText("Average Rating: (no reviews)");
            } else {
                avgRatingLabel.setText("Average Rating: " + refreshed.getFormattedRating());
            }

            List<Review> reviews = reviewDb.getReviewsForCourse(course.getId());
            rebuildReviewCards(reviews);

            myReview = reviewDb.getUserReview(userId, course.getId());
            syncEditorStateToMyReview();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to refresh:\n" + e.getMessage());
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }
}
