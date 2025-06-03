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

import java.sql.SQLException;

import databasePart1.DatabaseHelper;

/**
 * The AdminSetupPage class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
    
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	Label textLabel1 = new Label("Please enter your Administrator Credentials.");
	    textLabel1.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
	    textLabel1.setTranslateY(-20);
    	
    	// Input field for the admin's username
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        // Input field for the admin's password
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Input field for the admin's firstName
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter Admin First Name");
        firstNameField.setMaxWidth(250);

        // Input field for the admin's lastName
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Enter Admin Last Name");
        lastNameField.setMaxWidth(250);
        
        // Input field for the admin's email
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Admin email");
        emailField.setMaxWidth(250);

        // Button to complete the setup
        Button setupButton = new Button("Create Admin Account");
        setupButton.setTranslateY(20);
        
        setupButton.setOnAction(a -> {
            // Retrieve user input from the text fields
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            
            try {
                // Create a new User object with admin role including first name, last name, and email
                User user = new User(userName, password, "Admin", firstName, lastName, email);
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");
                
                // Navigate to the Welcome Login Page (assuming WelcomeLoginPage accepts a DatabaseHelper and a User)
                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Arrange all input fields and the setup button in a VBox layout
        VBox layout = new VBox(10,textLabel1, userNameField, passwordField, firstNameField, lastNameField, emailField, setupButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/admin.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true) // Scale image to fit
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));
        
        
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
