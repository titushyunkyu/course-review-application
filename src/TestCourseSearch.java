package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestCourseSearch extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DatabaseDriver.initialize();
        CourseSearchScene courseSearchScene = new CourseSearchScene(primaryStage, 1);
        Scene scene = courseSearchScene.createScene();
        primaryStage.setTitle("Course Review System - Course Search");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseDriver.closeConnection();
    }
}