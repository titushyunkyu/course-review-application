package edu.virginia.sde.reviews;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Optional;

public class UserLoginScene {

    private final Stage primaryStage;
    private final UserManagement userManagement = new UserManagement();

    private TextField usernameField;
    private PasswordField passwordField;
    private TextField visiblePasswordField;
    private boolean passwordVisible = false;
    private Label messageLabel;
    private Button togglePasswordButton;

    public UserLoginScene(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPrefSize(1050, 650);
        root.setStyle("-fx-background-color: #f7f1e3;");

        // Top-right Exit Button
        Button exit = new Button("Exit");
        exit.setOnAction(e -> Platform.exit());
        HBox top = new HBox(exit);
        top.setAlignment(Pos.TOP_RIGHT);
        top.setPadding(new Insets(10));
        root.setTop(top);

        // Center content
        VBox center = new VBox(12);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20));

        Label title = new Label("Welcome to CourseReviews");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Log in or create an account to review your courses.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        VBox form = new VBox(8);
        form.setMaxWidth(420);

        // Username Field
        Label userLabel = new Label("Username:");
        usernameField = new TextField();

        Label userHint = new Label("If this username doesn't exist, you can create a new account below.");
        userHint.setWrapText(true);
        userHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

        // Password Field
        Label passLabel = new Label("Password:");

        passwordField = new PasswordField();
        visiblePasswordField = new TextField();
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        StackPane passStack = new StackPane(passwordField, visiblePasswordField);
        HBox.setHgrow(passStack, Priority.ALWAYS);

        togglePasswordButton = new Button("Show");
        togglePasswordButton.setMinWidth(60);
        togglePasswordButton.setOnAction(e -> togglePasswordVisibility());

        HBox passRow = new HBox(5, passStack, togglePasswordButton);

        Label passHint = new Label("Password must be at least 8 characters (any letters, numbers, or symbols).");
        passHint.setWrapText(true);
        passHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

        // Message Label
        messageLabel = new Label("");
        messageLabel.setStyle("-fx-text-fill: #b22222;");

        // Buttons
        Button login = new Button("Log In");
        Button create = new Button("Create Account");

        login.setOnAction(e -> handleLogin());
        create.setOnAction(e -> handleCreate());

        HBox buttons = new HBox(20, login, create);
        buttons.setAlignment(Pos.CENTER);

        form.getChildren().addAll(
                userLabel, usernameField, userHint,
                passLabel, passRow, passHint
        );

        center.getChildren().addAll(title, subtitle, form, messageLabel, buttons);
        root.setCenter(center);

        return new Scene(root, 1050, 650);
    }

    private String getPasswordInput() {
        return passwordVisible ? visiblePasswordField.getText() : passwordField.getText();
    }

    private void clearPasswordFields() {
        passwordField.clear();
        visiblePasswordField.clear();
    }

    private void resetPasswordToHidden() {
        passwordVisible = false;

        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        passwordField.setVisible(true);
        passwordField.setManaged(true);

        togglePasswordButton.setText("Show");
    }

    private void togglePasswordVisibility() {
        if (!passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            togglePasswordButton.setText("Hide");
            passwordVisible = true;
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            togglePasswordButton.setText("Show");
            passwordVisible = false;
        }
    }

    private void handleCreate() {
        String username = usernameField.getText().trim();
        String password = getPasswordInput();

        if (username.isEmpty()) {
            messageLabel.setText("Username cannot be empty.");
            return;
        }
        if (password.length() < 8) {
            messageLabel.setText("Password must be at least 8 characters.");
            return;
        }

        Optional<User> created = userManagement.createUser(username, password);
        if (created.isEmpty()) {
            messageLabel.setText("Username already exists. Please choose another.");
        } else {
            messageLabel.setText("Account created. Please log in.");
            clearPasswordFields();
            resetPasswordToHidden();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = getPasswordInput();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        Optional<User> user = userManagement.findUsingUsernameAndPassword(username, password);
        if (user.isPresent()) {

            usernameField.clear();
            clearPasswordFields();
            resetPasswordToHidden();

            CourseSearchScene search = new CourseSearchScene(primaryStage, user.get().getId());
            primaryStage.setScene(search.createScene());
            primaryStage.setTitle("Course Reviews");

        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

}
