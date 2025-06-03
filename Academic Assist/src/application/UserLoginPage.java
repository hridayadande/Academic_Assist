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
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {

    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage, Scene previousScene) {
    	Label text = new Label("Login to your Account");
	    text.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
	    text.setTranslateY(-20);
    	
    	// Input field for the user's username and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Login button action
        Button loginButton = new Button("Login");
        loginButton.setOnAction(a -> {
            // Clear any previous error message
            errorLabel.setText("");

            // Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
            try { 
                // Updated: Create a new User object using empty strings for the additional parameters.
                // Note that the constructor now requires six parameters:
                // userName, password, role, firstName, lastName, email.
                User user = new User(userName, password, "", "", "", "");
                	
                
                // Retrieve the user's role from the database using userName
                String role = databaseHelper.getUserRole(userName);
                
             // Check if the password is only number and if it is correct password for the user than it is a otp
                if (password.matches("[0-9]+") && databaseHelper.isOTPValid(userName, password)) {
                    new UserPasswordReset(userName).show(primaryStage, primaryStage.getScene());
                    return;
                }
                
                else if (role != null) {
                    user.setRole(role);
                    // Attempt to log in the user with the given credentials
                    if (databaseHelper.login(user)) {
                        // Get user details from database to populate the User object
                        String firstName = databaseHelper.getUserFirstName(userName);
                        String lastName = databaseHelper.getUserLastName(userName);
                        String email = databaseHelper.getUserEmail(userName);
                        user = new User(userName, password, role, firstName, lastName, email);

                        if (role.contains(",")) {
                            // Multiple roles - show role selection page
                            new SelectRole().show(primaryStage, user, role);
                        } else {
                            // Single role - direct navigation
                            switch(role.trim()) {
                                case "Student":
                                    new StudentHomePage().show(primaryStage, user);
                                    break;
                                case "Staff":
                                    new StaffHomePage().show(primaryStage, user);
                                    break;
                                case "Reviewer":
                                    new ReviewerHomePage().show(primaryStage, user);
                                    break;
                                case "Instructor":
                                    new InstructorHomePage().show(primaryStage, user);
                                    break;
                                case "admin":
                                case "Admin":
                                    new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                                    break;
                                default:
                                    System.out.println("Unknown role: " + role);
                                    break;
                            }
                        }
                    } else {
                        // Display an error if the login fails
                        errorLabel.setText("Error logging in");
                    }
                } else {
                    // Display an error if the account does not exist
                    errorLabel.setText("User account doesn't exist");
                }
                
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Back button to return to the previous scene
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(text, userNameField, passwordField, loginButton, errorLabel, backButton);

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
        
        
        Scene loginScene = new Scene(layout, 800, 400);

        primaryStage.setScene(loginScene);
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
