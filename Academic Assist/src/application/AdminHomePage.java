package application;

import databasePart1.DatabaseHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

//adds for background
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class AdminHomePage {

    /**
     * Displays the admin page in the provided primary stage.
     * 
     * @param primaryStage   The primary stage where the scene will be displayed.
     * @param previousScene  The scene to return to when the back button is clicked.
     * @param adminUserName  The username of the currently logged in admin.
     */
	private final DatabaseHelper dbHelper = new DatabaseHelper();
	
    public void show(Stage primaryStage, Scene previousScene, String adminUserName) {
        BorderPane mainLayout = new BorderPane();
        VBox layout = new VBox(10);
        
        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/admin.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true)
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Welcome label
        Label adminLabel = new Label("Hello, " + adminUserName + "!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create admin actions section
        VBox adminActionsBox = new VBox(10);
        adminActionsBox.setPadding(new Insets(10));
        adminActionsBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");
        
        Label actionsTitle = new Label("Admin Actions");
        actionsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Create action buttons
        Button manageUsersButton = new Button("Manage Users");
        Button setOTPButton = new Button("Set OTP");
        Button inviteUsersButton = new Button("Invite New Users");
        Button viewReportsButton = new Button("View Reports");
        Button manageRolesButton = new Button("Manage User Roles");
        Button viewRequestsButton = new Button("View Admin Requests");
        
        // Add action buttons to the box
        adminActionsBox.getChildren().addAll(actionsTitle, manageUsersButton, setOTPButton, 
            inviteUsersButton, viewReportsButton, manageRolesButton, viewRequestsButton);

        
        // Create TableView to display user data
        TableView<User> userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Define table columns
        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getUserName()));

        TableColumn<User, String> roleColumn = new TableColumn<>("Roles");
        roleColumn.setCellValueFactory(cellData -> {
            String roles = cellData.getValue().getRole();
            roles = roles.replace(",", ", ");
            return new SimpleStringProperty(roles);
        });

        TableColumn<User, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getfirstName()));

        TableColumn<User, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getlastName()));

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getemail()));

        userTable.getColumns().addAll(usernameColumn, roleColumn, firstNameColumn, lastNameColumn, emailColumn);

        // Load users into the table
        Runnable loadUsers = () -> {
            userTable.getItems().clear();
            try {
                dbHelper.connectToDatabase();
                ObservableList<User> users = dbHelper.getAllUsers();
                userTable.setItems(users);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        loadUsers.run();

        // Action button handlers
        manageUsersButton.setOnAction(e -> {
            userTable.setVisible(!userTable.isVisible());
            manageUsersButton.setText(userTable.isVisible() ? "Hide User Management" : "Manage Users");
        });

        setOTPButton.setOnAction(e -> {
            AdminUserReset adminUserReset = new AdminUserReset();
            adminUserReset.show(primaryStage, primaryStage.getScene());
        });

        inviteUsersButton.setOnAction(e -> {
            new InvitationPage().show(dbHelper, primaryStage, primaryStage.getScene());
        });

        viewReportsButton.setOnAction(e -> {
            // TODO: Implement view reports functionality
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Reports feature coming soon!");
            alert.showAndWait();
        });

        manageRolesButton.setOnAction(e -> {
            // TODO: Implement role management functionality
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Role management feature coming soon!");
            alert.showAndWait();
        });

        // View Requests button action
        viewRequestsButton.setOnAction(e -> {
            new AdminAccessRequest(dbHelper, adminUserName).showAdminRequestsView(new Stage());
        });

        // Delete button
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a user to delete.");
                alert.showAndWait();
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Are you sure you want to delete user: " + selectedUser.getUserName() + "?", 
                    ButtonType.YES, ButtonType.NO);
            confirmation.showAndWait();
            if (confirmation.getResult() == ButtonType.YES) {
                boolean success = dbHelper.deleteUser(selectedUser.getUserName());
                if (success) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION, "User deleted successfully.");
                    info.showAndWait();
                    loadUsers.run();
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Failed to delete the user.");
                    error.showAndWait();
                }
            }
        });
        // Button to disable instructor's admin privileges
        Button disableAdminButton = new Button("Disable Admin Privileges");
        disableAdminButton.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a user with admin access.");
                alert.showAndWait();
                return;
            }
            
            if (!selectedUser.getRole().contains("Admin")) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Selected user does not have admin access.");
                alert.showAndWait();
                return;
            }

            try {
                // Get the current role string
                String currentRole = selectedUser.getRole();
                
                // Properly remove the Admin role
                String updatedRole = removeRole(currentRole, "Admin");
                
                // If the role becomes empty, set it to a default role
                if (updatedRole.isEmpty()) {
                    // If they had instructor role, keep it as instructor, otherwise student
                    if (currentRole.contains("Instructor")) {
                        updatedRole = "Instructor";
                    } else {
                        updatedRole = "Student"; 
                    }
                }
                
                System.out.println("Original role: " + currentRole);
                System.out.println("Updated role: " + updatedRole);
                
                // Update the user's role
                dbHelper.updateUserRole(selectedUser.getUserName(), updatedRole);
                
                // Add this action to closed requests
                String reason = "Admin privileges disabled by " + adminUserName;
                String currentDate = java.time.LocalDate.now().toString();
                dbHelper.addClosedAdminRequest(selectedUser.getUserName(), reason, currentDate);
                
                new Alert(Alert.AlertType.INFORMATION, "Admin privileges removed.").showAndWait();
                loadUsers.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to update role: " + ex.getMessage()).showAndWait();
            }
        });
        //Button to open the dialog that displays closed admin access requests
        Button viewClosedRequestsButton = new Button("View Closed Requests");
        viewClosedRequestsButton.setOnAction(e -> {
            try {
                ObservableList<Request> closedRequests = dbHelper.getClosedAdminRequests();
                
                if (closedRequests.isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION, "No closed requests found").showAndWait();
                    return;
                }
                
                Stage dialogStage = new Stage();
                dialogStage.setTitle("Closed Requests");
                
                VBox dialogLayout = new VBox(10);
                dialogLayout.setPadding(new Insets(20));
                
                Label titleLabel = new Label("Closed Admin Requests");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                
                TableView<Request> closedTable = new TableView<>(closedRequests);

                TableColumn<Request, String> userCol = new TableColumn<>("Username");
                userCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));
                
                // Show description in the Reason column to match View Admin Requests
                TableColumn<Request, String> reasonCol = new TableColumn<>("Reason");
                reasonCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
                reasonCol.setPrefWidth(350);
                
                TableColumn<Request, String> dateCol = new TableColumn<>("Date");
                dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
                dateCol.setPrefWidth(150);
                
                closedTable.getColumns().addAll(userCol, reasonCol, dateCol);
                
                Button closeButton = new Button("Close");
                closeButton.setOnAction(ev -> dialogStage.close());
                
                dialogLayout.getChildren().addAll(
                    titleLabel, 
                    closedTable, 
                    closeButton
                );
                
                Scene dialogScene = new Scene(dialogLayout, 700, 400);
                dialogStage.setScene(dialogScene);
                dialogStage.show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to load closed requests: " + ex.getMessage()).showAndWait();
            }
        });        
        
        adminActionsBox.getChildren().addAll(disableAdminButton, viewClosedRequestsButton);

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        // Add components to layouts
        layout.getChildren().addAll(adminLabel, adminActionsBox, userTable, deleteButton, backButton);
        mainLayout.setCenter(layout);

        Scene adminScene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
        
        
    }
    
    // Helper method to properly remove a role from a comma-separated list of roles
    private String removeRole(String roleString, String roleToRemove) {
        // Split the role string by commas
        String[] roles = roleString.split(",");
        StringBuilder newRoleString = new StringBuilder();
        
        for (String role : roles) {
            role = role.trim();
            if (!role.equals(roleToRemove)) {
                if (newRoleString.length() > 0) {
                    newRoleString.append(",");
                }
                newRoleString.append(role);
            }
        }
        
        return newRoleString.toString();
    }
}