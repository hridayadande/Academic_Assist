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
import databasePart1.*;







public class SetupAccountPage {
    
    private final DatabaseHelper databaseHelper;
    private final Stage primaryStage;
    // Changed type from WelcomeLoginPage to SetupLoginSelectionPage 
    private final SetupLoginSelectionPage previousPage;

    // Updated constructor to require the previous page as a SetupLoginSelectionPage
    public SetupAccountPage(DatabaseHelper databaseHelper, Stage primaryStage, SetupLoginSelectionPage previousPage) {
        this.databaseHelper = databaseHelper;
        this.primaryStage = primaryStage;
        this.previousPage = previousPage;
    }

    public void show() {
    	Label text = new Label("Please Enter the Following Info to make an Account");
	    text.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
	    text.setTranslateY(-40);
    	
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter First Name");
        firstNameField.setMaxWidth(250);
        
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Enter Last Name");
        lastNameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);
        
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setMaxWidth(250);
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Button setupButton = new Button("Setup");
        setupButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            
            String uservalidationMessage = UserNameRecognizer.checkForValidUserName(userName);
            if (!uservalidationMessage.isEmpty()) {
                errorLabel.setText(uservalidationMessage);
                return;
            }
            
            String passvalidationMessage = PasswordEvaluator.evaluatePassword(password);
            if (!passvalidationMessage.isEmpty()) {
                errorLabel.setText(passvalidationMessage);
                return;
            }
            
            String namevalidationMessage = NameRecognizer.nameEvaluator(firstName, lastName);
            if (!namevalidationMessage.isEmpty()) {
                errorLabel.setText(namevalidationMessage);
                return;
            }
            
            String emailvalidationMessage = EmailRecognizer.emailEvaluator(email);
            if (!emailvalidationMessage.isEmpty()) {
                errorLabel.setText(emailvalidationMessage);
                return;
            }
            
            try {
                if (!databaseHelper.doesUserExist(userName)) {
                    // Get the role associated with the invitation code
                    String role = databaseHelper.validateInvitationCodeAndGetRole(code);
                    if (role != null) {
                        // Create user with the role from the invitation code
                        User user = new User(userName, password, role, firstName, lastName, email);
                        databaseHelper.register(user);
                        // After a successful setup, navigate to WelcomeLoginPage.
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                    } else {
                        errorLabel.setText("Please enter a valid invitation code");
                    }
                } else {
                    errorLabel.setText("This username is taken! Please use another.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Create the Back button and use the previousPage reference to navigate back.
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            if (previousPage != null) {
                previousPage.show(primaryStage);
            }
        });

        
        
     
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/setup.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100,100, true, true, true, true) // Scale image to fit
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));
        
        layout.getChildren().addAll(
            text,
        	firstNameField, 
            lastNameField, 
            emailField,
            userNameField, 
            passwordField, 
            inviteCodeField, 
            setupButton, 
            backButton, // Back button added here
            errorLabel
        );

        primaryStage.setScene(new Scene(layout, 800, 600));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}