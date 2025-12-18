package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

//Course search scene, main hub for the application
public class CourseSearchScene {
    private Stage primaryStage;
    private Connection connection;
    private int currentUserId;
    private CourseDatabaseManager courseDatabaseManager;

    //UI Components
    private TableView<Course> courseTable;
    private ObservableList<Course> courseList;
    private TextField subjectSearchField;
    private TextField numberSearchField;
    private TextField titleSearchField;

    //Add course fields
    private TextField subjectAddField;
    private TextField numberAddField;
    private TextField titleAddField;
    private Label errorLabel;

    public CourseSearchScene(Stage primaryStage, int currentUserId) {
        this.primaryStage = primaryStage;
        this.currentUserId = currentUserId;
        try {
            this.connection = DatabaseDriver.getConnection();
            this.courseDatabaseManager = new CourseDatabaseManager(connection);
            this.courseList = FXCollections.observableArrayList();
        }
        catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to connect to database: " + e.getMessage());
        }
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        root.setTop(createTopSection());

        root.setCenter(createCenterSection());

        root.setBottom(createBottomSection());

        loadAllCourses();

        Scene scene = new Scene(root, 1050, 650);
        return scene;
    }
    //Top section with title and navigation buttons
    private VBox createTopSection() {
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(0, 0, 15, 0));

        //Title
        Label titleLabel = new Label("Course Search");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        //Navigation buttons
        HBox navBox = new HBox(10);
        navBox.setAlignment(Pos.CENTER_RIGHT);

        Button myReviewsButton = new Button("My Reviews");
        myReviewsButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        myReviewsButton.setOnAction(e -> navigateToMyReviews());

        Button logoutButton = new Button("Log Out");
        logoutButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        logoutButton.setOnAction(e -> logout());

        navBox.getChildren().addAll(myReviewsButton, logoutButton);

        topBox.getChildren().addAll(titleLabel, navBox);
        return topBox;
    }
    //Center section with search and table
    private VBox createCenterSection() {
        VBox centerBox = new VBox(15);

        centerBox.getChildren().add(createSearchSection());

        centerBox.getChildren().add(createCourseTable());

        return centerBox;
    }
    //Search section with filters
    private VBox createSearchSection() {
        VBox searchBox = new VBox(10);
        searchBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 15; -fx-background-radius: 5;");

        Label searchLabel = new Label("Search Courses");
        searchLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        //Search fields in a grid
        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);

        //Subject search
        Label subjectLabel = new Label("Subject:");
        subjectSearchField = new TextField();
        subjectSearchField.setPromptText("e.g., CS");
        subjectSearchField.setPrefWidth(100);

        //Number search
        Label numberLabel = new Label("Number:");
        numberSearchField = new TextField();
        numberSearchField.setPromptText("e.g., 3140");
        numberSearchField.setPrefWidth(100);

        //Title search
        Label titleLabel = new Label("Title:");
        titleSearchField = new TextField();
        titleSearchField.setPromptText("e.g., Software");
        titleSearchField.setPrefWidth(300);

        //Search and clear buttons
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 20;");
        searchButton.setOnAction(e -> performSearch());

        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 20;");
        clearButton.setOnAction(e -> clearSearch());

        //Add to grid
        searchGrid.add(subjectLabel, 0, 0);
        searchGrid.add(subjectSearchField, 1, 0);
        searchGrid.add(numberLabel, 2, 0);
        searchGrid.add(numberSearchField, 3, 0);
        searchGrid.add(titleLabel, 4, 0);
        searchGrid.add(titleSearchField, 5, 0);
        searchGrid.add(searchButton, 6, 0);
        searchGrid.add(clearButton, 7, 0);

        searchBox.getChildren().addAll(searchLabel, searchGrid);
        return searchBox;
    }
    //Course table with columns
    private TableView<Course> createCourseTable() {
        courseTable = new TableView<>();
        courseTable.setItems(courseList);
        courseTable.setPrefHeight(350);

        //Subject column
        TableColumn<Course, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        subjectCol.setPrefWidth(100);

        //Number column
        TableColumn<Course, Integer> numberCol = new TableColumn<>("Number");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        numberCol.setPrefWidth(100);

        //Title column
        TableColumn<Course, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(400);

        //Average Rating column
        TableColumn<Course, String> ratingCol = new TableColumn<>("Avg Rating");
        ratingCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFormattedRating()
                )
        );
        ratingCol.setPrefWidth(100);

        //Action column with view button
        TableColumn<Course, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View Reviews");

            {
                viewButton.setOnAction(event -> {
                    Course course = getTableView().getItems().get(getIndex());
                    navigateToCourseReviews(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                }
                else {
                    setGraphic(viewButton);
                }
            }
        });

        courseTable.getColumns().addAll(subjectCol, numberCol, titleCol, ratingCol, actionCol);

        //Enable row selection as alternative to button
        courseTable.setRowFactory(tv -> {
            TableRow<Course> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Course course = row.getItem();
                    navigateToCourseReviews(course);
                }
            });
            return row;
        });

        return courseTable;
    }
    //Bottom section for adding courses
    private VBox createBottomSection() {
        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        bottomBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 15; -fx-background-radius: 5;");

        Label addLabel = new Label("Add New Course");
        addLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        //Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setVisible(false);

        //Input fields
        GridPane addGrid = new GridPane();
        addGrid.setHgap(10);
        addGrid.setVgap(10);

        Label subjectAddLabel = new Label("Subject (2-4 letters):");
        subjectAddField = new TextField();
        subjectAddField.setPromptText("e.g., CS");
        subjectAddField.setPrefWidth(100);

        Label numberAddLabel = new Label("Number (4 digits):");
        numberAddField = new TextField();
        numberAddField.setPromptText("e.g., 3140");
        numberAddField.setPrefWidth(100);

        Label titleAddLabel = new Label("Title (1-50 chars):");
        titleAddField = new TextField();
        titleAddField.setPromptText("e.g., Software Development Essentials");
        titleAddField.setPrefWidth(300);

        Button addButton = new Button("Add Course");
        addButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 20; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        addButton.setOnAction(e -> addCourse());

        addGrid.add(subjectAddLabel, 0, 0);
        addGrid.add(subjectAddField, 1, 0);
        addGrid.add(numberAddLabel, 2, 0);
        addGrid.add(numberAddField, 3, 0);
        addGrid.add(titleAddLabel, 4, 0);
        addGrid.add(titleAddField, 5, 0);
        addGrid.add(addButton, 6, 0);

        bottomBox.getChildren().addAll(addLabel, errorLabel, addGrid);
        return bottomBox;
    }
    //Load all courses from database
    private void loadAllCourses() {
        try {
            courseList.clear();
            courseList.addAll(courseDatabaseManager.getAllCourses());
        }
        catch (SQLException e) {
            showError("Database Error", "Failed to load courses: " + e.getMessage());
        }
    }
    //Perform search based on filters
    private void performSearch() {
        String subject = subjectSearchField.getText().trim();
        String number = numberSearchField.getText().trim();
        String title = titleSearchField.getText().trim();

        if (subject.isEmpty() && number.isEmpty() && title.isEmpty()) {
            loadAllCourses();
            return;
        }

        //Validate number if provided
        if (!number.isEmpty() && !ValidationUtils.isValidNumber(number)) {
            showError("Invalid Input", "Course number must be exactly 4 digits");
            return;
        }

        try {
            courseList.clear();
            courseList.addAll(courseDatabaseManager.searchCourses(
                    subject.isEmpty() ? null : subject,
                    number.isEmpty() ? null : number,
                    title.isEmpty() ? null : title
            ));
        }
        catch (SQLException e) {
            showError("Database Error", "Failed to search courses: " + e.getMessage());
        }
    }

    private void clearSearch() {
        subjectSearchField.clear();
        numberSearchField.clear();
        titleSearchField.clear();
        loadAllCourses();
    }
    //Add a new course to the database
    private void addCourse() {
        String subject = subjectAddField.getText().trim();
        String number = numberAddField.getText().trim();
        String title = titleAddField.getText().trim();

        //Validate subject
        if (!ValidationUtils.isValidSubject(subject)) {
            showErrorInline(ValidationUtils.getSubjectErrorMessage());
            return;
        }

        //Validate number
        if (!ValidationUtils.isValidNumber(number)) {
            showErrorInline(ValidationUtils.getNumberErrorMessage());
            return;
        }

        //Validate title
        if (!ValidationUtils.isValidTitle(title)) {
            showErrorInline(ValidationUtils.getTitleErrorMessage());
            return;
        }

        try {
            Course newCourse = new Course(
                    ValidationUtils.capitalizeSubject(subject),
                    Integer.parseInt(number),
                    title.trim()
            );

            boolean success = courseDatabaseManager.addCourse(newCourse);

            if (success) {
                subjectAddField.clear();
                numberAddField.clear();
                titleAddField.clear();
                errorLabel.setVisible(false);

                //Refresh course list
                loadAllCourses();

                showSuccess("Success", "Course added successfully!");
            }
            else {
                showErrorInline("This course already exists in the database.");
            }
        }
        catch (SQLException e) {
            showError("Database Error", "Failed to add course: " + e.getMessage());
        }
    }

    //Show inline error message in the add course section
    private void showErrorInline(String message) {
        errorLabel.setText("Error: " + message);
        errorLabel.setVisible(true);
    }

    //Show error dialog
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //Show success dialog
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //Navigate to course reviews scene
    private void navigateToCourseReviews(Course course) {
        try {
            CourseReviewsScene reviewsScene =
                    new CourseReviewsScene(primaryStage, currentUserId, course);

            primaryStage.setScene(reviewsScene.createScene());
            primaryStage.setTitle("Course Reviews");

        } catch (SQLException e) {
            showError("Database Error",
                    "Failed to load course reviews:\n" + e.getMessage());
        }
    }

    //Navigate to my reviews scene
    private void navigateToMyReviews() {
        // TODO: Navigate to my reviews scene
        MyReviewsScene myReviewsScene = new MyReviewsScene(primaryStage, currentUserId);
        primaryStage.setScene(myReviewsScene.createScene());
        primaryStage.setTitle("Course Reviews");
    }

    //Log out and return to login screen
    private void logout() {
        currentUserId = -1;

        UserLoginScene login = new UserLoginScene(primaryStage);
        primaryStage.setScene(login.createScene());
        primaryStage.setTitle("Course Reviews");
    }

}