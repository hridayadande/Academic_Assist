package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
    
    private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage, User user) {
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/background.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true) // Scale image to fit
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));
        
        
        Label welcomeLabel = new Label("Welcome, " + user.getfirstName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        welcomeLabel.setTranslateY(-25);
        
        Label instLabel = new Label("Select if you would like to Continue to your User Page");
        instLabel.setStyle("-fx-font-size: 16px;");
        
        
        Label instLabel2 = new Label("or Invite new Users to Join");
        instLabel2.setStyle("-fx-font-size: 16px;");
        instLabel2.setTranslateY(-5);
        
        
        // Scene setup before navigating to other pages
        Scene welcomeScene = new Scene(layout, 800, 400);
        
        // Button to navigate to the user's respective page based on their role
        Button continueButton = new Button("User Page");
        continueButton.setOnAction(a -> {
            String role = user.getRole().toLowerCase();
            
            if (role.contains("admin")) {
                new AdminHomePage().show(primaryStage, welcomeScene, user.getUserName());
            } else {
                // For users with multiple roles, show the role selection page
                new SelectRole().show(primaryStage, user, role);
            }
        });

        // Logout Button to return to the login selection page
        Button logoutButton = new Button("Logout");
        logoutButton.setTranslateX(-30);
        logoutButton.setTranslateY(30);
        logoutButton.setOnAction(a -> {
            databaseHelper.closeConnection(); // Close DB connection
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage); // Redirect to login selection
        });

        // Button to quit the application
        Button quitButton = new Button("Quit");
        quitButton.setTranslateX(28);
        quitButton.setTranslateY(-5);
        quitButton.setOnAction(a -> {
            databaseHelper.closeConnection();
            Platform.exit(); // Exit the JavaFX application
        });
        
        

        layout.getChildren().addAll(welcomeLabel, instLabel, instLabel2, continueButton);
        
        layout.getChildren().addAll(logoutButton, quitButton);

        
        // Set the scene to primary stage
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
    }
}
