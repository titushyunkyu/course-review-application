package edu.virginia.sde.reviews;
import javafx.stage.Stage;

public final class AppContext {
    private static Stage primaryStage;

    private AppContext() {}

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        if (primaryStage == null){
            throw new IllegalArgumentException("Primary stage not set in AppContext");
        }
        return primaryStage;
    }
}
