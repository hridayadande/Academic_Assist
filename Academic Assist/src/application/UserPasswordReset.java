package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

public class UserPasswordReset {

    private final String userName;

    public UserPasswordReset(String userName) {
        this.userName = userName;
    }

    public void show(Stage primaryStage, Scene previousScene) {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("Reset Password for " + userName);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter new password");
        passwordField.setMaxWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        //to reset the password also checks with the passwordevaluator
        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.setOnAction(e -> {
            String newPassword = passwordField.getText();
            String validationMessage = PasswordEvaluator.evaluatePassword(newPassword);
            
            if (!validationMessage.isEmpty()) {
                errorLabel.setText("Invalid password: " + validationMessage);
                return;
            }
            
            try {
                databaseHelper.updatePassword(userName, newPassword);
                primaryStage.setScene(previousScene);
            } catch (SQLException ex) {
                errorLabel.setText("Database error: Unable to update password.");
                ex.printStackTrace();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        layout.getChildren().addAll(titleLabel, passwordField, resetPasswordButton, errorLabel, backButton);
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("User Password Reset");
    }
}
