package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.application.Platform;

public class CourseReviewsApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        // Setup database tables
        try {
            DatabaseDriver.initialize();
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.ERROR, "Database failed to initialize:\n" + ex.getMessage()).showAndWait();
            Platform.exit();
            return;
        }

        AppContext.setPrimaryStage(stage);

        UserLoginScene login = new UserLoginScene(stage);
        stage.setScene(login.createScene());
        stage.setTitle("Course Reviews");
        stage.show();
        }

    @Override
    public void stop(){
        DatabaseDriver.closeConnection();
    }

}
