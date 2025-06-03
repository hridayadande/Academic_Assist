package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the user.
 */
public class UserHomePage {

    /**
     * Displays the user page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param previousScene The scene to return to when back button is clicked.
     */
    public void show(Stage primaryStage, Scene previousScene) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Label to display Hello user
        Label userLabel = new Label("Hello, User!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Back button to return to the previous scene
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        layout.getChildren().addAll(userLabel, backButton);
        Scene userScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
    }
}
