package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import databasePart1.DatabaseHelper;

/**
 * This class displays a dialog showing admin actions that staff and instructors 
 * can request access to perform.
 */
public class AdminActionsDialog {
    private User currentUser;
    private DatabaseHelper dbHelper;
    
    /**
     * Constructor for AdminActionsDialog
     * 
     * @param currentUser The current user
     * @param dbHelper The database helper instance
     */
    public AdminActionsDialog(User currentUser, DatabaseHelper dbHelper) {
        this.currentUser = currentUser;
        this.dbHelper = dbHelper;
    }
    
    /**
     * Show the dialog displaying admin actions
     */
    public void show() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Admin Actions");
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white;");
        
        Label titleLabel = new Label("Available Admin Actions");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // List of admin actions from AdminHomePage
        ListView<String> actionsList = new ListView<>();
        actionsList.getItems().addAll(
            "Manage Users",
            "Set OTP",
            "Invite New Users",
            "View Reports",
            "Manage User Roles",
            "View Admin Requests"
        );
        
        // Add descriptions for each action
        TextArea descriptionArea = new TextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        
        // Show description when an action is selected
        actionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "Manage Users":
                        descriptionArea.setText("View and manage all users in the system. " +
                                "Administrators can delete users when necessary.");
                        break;
                    case "Set OTP":
                        descriptionArea.setText("Set one-time passwords for users who need " +
                                "password resets.");
                        break;
                    case "Invite New Users":
                        descriptionArea.setText("Generate invitation codes to allow new users " +
                                "to register with specific roles.");
                        break;
                    case "View Reports":
                        descriptionArea.setText("Access system reports and analytics.");
                        break;
                    case "Manage User Roles":
                        descriptionArea.setText("Change or update user roles in the system.");
                        break;
                    case "View Admin Requests":
                        descriptionArea.setText("View and approve/deny requests for admin access.");
                        break;
                    default:
                        descriptionArea.setText("");
                }
            }
        });
        
        // Set initial selection
        actionsList.getSelectionModel().selectFirst();
        
        Label noteLabel = new Label("Note: These actions require admin privileges. " +
                "You can request access to perform these actions.");
        noteLabel.setWrapText(true);
        
        // Buttons
        Button requestAccessButton = new Button("Request Admin Access");
        requestAccessButton.setOnAction(e -> {
            new AdminAccessRequest(dbHelper, currentUser.getUserName()).showRequestDialog(new Stage());
            dialogStage.close();
        });
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialogStage.close());
        
        // Check if user already has a pending admin request
        try {
            boolean hasPendingRequest = dbHelper.hasUserRequestedAdminAccess(currentUser.getUserName());
            
            if (hasPendingRequest) {
                requestAccessButton.setDisable(true);
                requestAccessButton.setText("Request Pending");
                noteLabel.setText("Note: You already have a pending admin access request. " +
                        "Please wait for an existing admin to respond.");
            } else if (dbHelper.isUserRequestApproved(currentUser.getUserName())) {
                // If the request was approved but the role doesn't include admin yet (user needs to re-login)
                if (!currentUser.getRole().contains("admin")) {
                    requestAccessButton.setDisable(true);
                    requestAccessButton.setText("Request Approved");
                    noteLabel.setText("Your admin access request has been approved! " +
                            "Please log out and log back in to access admin features.");
                } else {
                    // User already has admin role
                    requestAccessButton.setDisable(true);
                    requestAccessButton.setText("You Have Admin Access");
                    noteLabel.setText("You already have admin access. " +
                            "You can access the admin page from the role selection screen.");
                }
            }
        } catch (Exception ex) {
            // In case of error, still allow requesting
        }
        
        layout.getChildren().addAll(
            titleLabel,
            new Label("Available Admin Actions:"),
            actionsList,
            new Label("Description:"),
            descriptionArea,
            noteLabel,
            requestAccessButton,
            closeButton
        );
        
        Scene scene = new Scene(layout, 450, 500);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
} 