package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class SelectRole {
    
    public void show(Stage primaryStage, User user, String roleString) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/role.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true) // Scale image to fit
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));
        
        // Create label
        Label selectLabel = new Label("Select your role:");
        selectLabel.setStyle("-fx-font-size: 14px;");
        
        // Create ComboBox for role selection
        ComboBox<String> roleComboBox = new ComboBox<>();
        
        // Split the roleString and add each role to the ComboBox
        String[] roles = roleString.split(",");
        for (String role : roles) {
            String trimmedRole = role.trim();
            // Don't add Restricted as a selectable role
            if (!trimmedRole.equals("Restricted")) {
                roleComboBox.getItems().add(trimmedRole);
            }
        }
        
        // Select first role by default
        if (roles.length > 0) {
            roleComboBox.setValue(roles[0].trim());
        }
        
        // If the user has the Restricted flag, show a message
        if (roleString.contains("Restricted")) {
            Label restrictedLabel = new Label("Note: Your account is restricted");
            restrictedLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
            layout.getChildren().add(restrictedLabel);
        }
        
        // Continue button
        Button continueButton = new Button("Continue");
        continueButton.setOnAction(e -> {
            String selectedRole = roleComboBox.getValue();
            if (selectedRole != null) {
                // Create a temporary user with the selected role and preserve any special flags like Restricted
                String fullRole = selectedRole;
                
                // Add Restricted flag if original role contained it
                if (roleString.contains("Restricted") && !selectedRole.equals("Restricted")) {
                    fullRole = selectedRole + ",Restricted";
                    System.out.println("Preserving restricted status for " + user.getUserName() + " with role: " + fullRole);
                }
                
                User tempUser = new User(
                    user.getUserName(),
                    user.getPassword(),
                    fullRole,
                    user.getfirstName(),
                    user.getlastName(),
                    user.getemail()
                );
                
                switch(selectedRole.trim()) {
                    case "Student":
                    case "student":
                        new StudentHomePage().show(primaryStage, tempUser);
                        break;
                    case "Staff":
                    case "staff":
                        new StaffHomePage().show(primaryStage, tempUser);
                        break;
                    case "Reviewer":
                    case "reviewer":
                        new ReviewerHomePage().show(primaryStage, tempUser);
                        break;
                    case "Instructor":
                    case "instructor":
                        new InstructorHomePage().show(primaryStage, tempUser);
                        break;
                    case "admin":
                    case "Admin":
                        // Get current scene to pass as previous scene
                        Scene currentScene = primaryStage.getScene();
                        new AdminHomePage().show(primaryStage, currentScene, tempUser.getUserName());
                        break;
                    case "Restricted":
                        // If somehow Restricted is selected as a role, redirect to Student with restriction
                        tempUser = new User(
                            user.getUserName(),
                            user.getPassword(),
                            "Student,Restricted",
                            user.getfirstName(),
                            user.getlastName(),
                            user.getemail()
                        );
                        new StudentHomePage().show(primaryStage, tempUser);
                        break;
                    default:
                        System.out.println("Unknown role: " + selectedRole);
                        break;
                }
            }
        });
        
        // Back button to return to login page
        Button backButton = new Button("Back");
        DatabaseHelper dbHelper = new DatabaseHelper();
        backButton.setOnAction(e -> new WelcomeLoginPage(dbHelper).show(primaryStage, user));
        
        layout.getChildren().addAll(selectLabel, roleComboBox, continueButton, backButton);
        
        Scene roleScene = new Scene(layout, 400, 300);
        primaryStage.setScene(roleScene);
        primaryStage.setTitle("Select Your Role");
    }
}
