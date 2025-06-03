package application;

import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.Image;

/**
 * InvitationPage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */
public class InvitationPage {

    /**
     * Displays the Invite Page in the provided primary stage.
     * 
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param primaryStage   The primary stage where the scene will be displayed.
     * @param previousScene  The scene to return to when the back button is clicked.
     */

public void show(DatabaseHelper databaseHelper, Stage primaryStage, Scene previousScene) {
    	VBox layout = new VBox(10);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    
	     // Load background image
        Image backgroundImage = new Image(getClass().getResource("/invite.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true) // Scale image to fit
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));

	    
	    // Label to display the title of the page
	    Label userLabel = new Label("Invite");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    /*//edits
	    Label roleField = new TextField();
        roleField.setPromptText("Enter Role Name");
        roleField.setMaxWidth(250);*/

	    TextField expDateField = new TextField();
        expDateField.setPromptText("Enter Expiration Date (YYYY/MM/DD)");
        expDateField.setMaxWidth(250);
	    //end edits
	    
	    VBox roleSelect = new VBox(10);
	    layout.setStyle("-fx-padding: 20;");
	    
	    Label roleLabel = new Label("Select Roles");
	    
	    // Create checkboxes instead of radio buttons
	    CheckBox studentCB = new CheckBox("Student");
	    CheckBox reviewerCB = new CheckBox("Reviewer");
	    CheckBox instructorCB = new CheckBox("Instructor");
	    CheckBox staffCB = new CheckBox("Staff");
	    
	    roleSelect.getChildren().addAll(roleLabel, studentCB, reviewerCB, instructorCB, staffCB);
	    
	    
	    // Button to generate the invitation code
	    Button showCodeButton = new Button("Generate Invitation Code");
	    
	    // Label to display the generated invitation code
	    Label inviteCodeLabel = new Label("");
	    inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
	    
	    showCodeButton.setOnAction(a -> {
	        // Collect all selected roles
	        StringBuilder selectedRoles = new StringBuilder();
	        if (studentCB.isSelected()) selectedRoles.append("Student,");
	        if (reviewerCB.isSelected()) selectedRoles.append("Reviewer,");
	        if (instructorCB.isSelected()) selectedRoles.append("Instructor,");
	        if (staffCB.isSelected()) selectedRoles.append("Staff,");
	        
	        // Remove trailing comma if exists
	        String roles = selectedRoles.toString();
	        if (roles.endsWith(",")) {
	            roles = roles.substring(0, roles.length() - 1);
	        }
	        
	        if (roles.isEmpty()) {
	            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select at least one role.");
	            alert.showAndWait();
	            return;
	        }
	        
	        // Generate the invitation code using the databaseHelper
	        String invitationCode = databaseHelper.generateInvitationCodeWithRole(roles);
	        if (invitationCode != null) {
	            inviteCodeLabel.setText("Code: " + invitationCode + " (Roles: " + roles + ")");
	        } else {
	            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to generate invitation code.");
	            alert.showAndWait();
	        }
	    });

        // Back button to return to the previous scene
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        layout.getChildren().addAll(userLabel, showCodeButton, inviteCodeLabel, roleSelect, expDateField, backButton);
        Scene inviteScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(inviteScene);
        primaryStage.setTitle("Invite Page");
    }
}