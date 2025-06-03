package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Random;

public class AdminUserReset {

    public void show(Stage primaryStage, Scene previousScene) {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("Reset User Password");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> userDropdown = new ComboBox<>();
        userDropdown.setPromptText("Select a user");

        // Load users into dropdown
        try {
            databaseHelper.connectToDatabase();
            ObservableList<User> users = databaseHelper.getAllUsers();
            for (User user : users) {
                userDropdown.getItems().add(user.getUserName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button generateOtpButton = new Button("Generate OTP");
        Label otpLabel = new Label();
        otpLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        generateOtpButton.setOnAction(e -> {
            String selectedUser = userDropdown.getValue();
            if (selectedUser != null) {
                String otp = generateOTP();
                otpLabel.setText("Generated OTP: " + otp);
                try {
                    databaseHelper.updatePassword(selectedUser, otp);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));	

        layout.getChildren().addAll(titleLabel, userDropdown, generateOtpButton, otpLabel, backButton);
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin User Reset");
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates a 6-digit OTP
        return String.valueOf(otp);
    }
}
