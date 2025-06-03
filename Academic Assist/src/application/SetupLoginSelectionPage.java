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
import databasePart1.*;

/**
 * The SetupLoginSelectionPage class allows users to choose between setting up a new account
 * or logging into an existing account. It provides two buttons for navigation to the respective pages.
 */
public class SetupLoginSelectionPage {
    
    private final DatabaseHelper databaseHelper;

    public SetupLoginSelectionPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Create a VBox layout
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        
        
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
        
        // Prepare the scene for this page
        Scene selectionScene = new Scene(layout, 800, 400);
        
        Label welcome = new Label("Welcome");
	    welcome.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
	    welcome.setTranslateY(-40);
	    
	    Label textbox = new Label("If you have an Account, please sign in.");
	    textbox.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
	    textbox.setTranslateY(-20);
	    Label textbox2 = new Label("Otherwise, contact your Administrator to get an Invitiation Code.");
	    textbox2.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
	    textbox2.setTranslateY(-20);
        
        // Buttons to select Login / Setup options that redirect to respective pages
        Button setupButton = new Button("Setup Account Using Invitation Code");
        Button loginButton = new Button("Login to Existing Account");

        // Setup button action: pass this page as the previous page reference 
        setupButton.setOnAction(a -> {
            new SetupAccountPage(databaseHelper, primaryStage, this).show();
        });

        // Login button action
        loginButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage, selectionScene); // Pass the current scene so the login page can navigate back if needed.
        });

        layout.getChildren().addAll(welcome, textbox, textbox2, loginButton, setupButton);

        primaryStage.setScene(selectionScene);
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
