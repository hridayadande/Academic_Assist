package application;

import databasePart1.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FirstPage class represents the initial screen for the first user.
 * It prompts the user to set up administrator access and navigate to the setup screen.
 */
public class FirstPage {
	
	// Reference to the DatabaseHelper for database interactions
	private final DatabaseHelper databaseHelper;
	public FirstPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

	/**
     * Displays the first page in the provided primary stage. 
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	VBox layout = new VBox(5);
    	
    	// Label to display the welcome message for the first user
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-background-color: #F1F1F1");
	    layout.setAlignment(Pos.CENTER);
	    Label welcome = new Label("Welcome");
	    welcome.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
	    welcome.setTranslateY(-30);
	    
	    Label textLabel1 = new Label("You are the first person here.");
	    textLabel1.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
	    
	    Label textLabel2 = new Label("Please select \"Begin Setup\" to setup administrator access.");
	    textLabel2.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
	    
	    Button beginButton = new Button("Begin Setup");
	    // Button to navigate to the SetupAdmin page
	    beginButton.setTranslateY(25);
	    
	    Label errorLabel = new Label("If you believe this is an error, please contact your administrator.");
	    errorLabel.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
	    errorLabel.setTranslateY(50);
	    
	    beginButton.setOnAction(a -> {
	        new AdminSetupPage(databaseHelper).show(primaryStage);
	        
	    });

	    layout.getChildren().addAll(welcome, textLabel1, textLabel2, beginButton, errorLabel);
	    Scene firstPageScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(firstPageScene);
	    primaryStage.setTitle("Start Page");
    	primaryStage.show();
    }
}