package application;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * The Reviewer class handles all operations related to reviews including
 * adding reviews, viewing reviews, and displaying review dialogs.
 */
public class Reviewer {
    private DatabaseHelper2 dbHelper2;
    private DatabaseHelper3 dbHelper3;
    
    /**
     * Initializes a new Reviewer instance with a database helper.
     */
    public Reviewer() {
        this.dbHelper2 = new DatabaseHelper2();
        this.dbHelper3 = new DatabaseHelper3();
        try {
            dbHelper2.connectToDatabase();
            dbHelper3.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a review to a question.
     * 
     * @param questionID The ID of the question being reviewed
     * @param user The reviewer
     */
    public void addReviewToQuestion(int questionID, User user) {
        try {
            // Get question details
            Question question = dbHelper2.getQuestionById(questionID);
            if (question == null) {
                showAlert("Question not found.", AlertType.ERROR);
                return;
            }
            
            showReviewDialog("Add Review for Question", 
                    "Question: " + question.getBodyText(), 
                    questionID, 
                    0, // answerID = 0 means it's a question review
                    user);
            
        } catch (SQLException e) {
            showAlert("Error retrieving question details: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Adds a review to an answer.
     * 
     * @param answer The answer being reviewed
     * @param user The reviewer
     */
    public void addReviewToAnswer(Answer answer, User user) {
        try {
            showReviewDialog("Add Review for Answer", 
                    "Answer: " + answer.getBodyText(), 
                    answer.getQuestionID(), 
                    answer.getAnsID(),
                    user);
            
        } catch (Exception e) {
            showAlert("Error creating review: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Displays a dialog for creating a new review.
     */
    private void showReviewDialog(String title, String header, int questionID, int answerID, User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Your review:");
        
        dialog.showAndWait().ifPresent(reviewText -> {
            if (!reviewText.trim().isEmpty()) {
                try {
                    // Insert the review
                    dbHelper3.insertReview(
                            questionID,
                            answerID,
                            user.getUserName(),
                            reviewText,
                            new Date()
                    );
                    showAlert("Review submitted successfully!", AlertType.INFORMATION);
                } catch (SQLException ex) {
                    showAlert("Error saving review: " + ex.getMessage(), AlertType.ERROR);
                }
            } else {
                showAlert("Review text cannot be empty", AlertType.WARNING);
            }
        });
    }
    
    /**
     * Updates an existing review.
     * 
     * @param reviewID The ID of the review to update
     * @param currentReviewText The current text of the review
     * @param userName The username of the current user
     */
    public void updateReview(int reviewID, String currentReviewText, String userName) {
        try {
            // First verify this review belongs to the current user
            if (!dbHelper3.isReviewOwner(reviewID, userName)) {
                showAlert("You can only update your own reviews.", AlertType.WARNING);
                return;
            }
            
            TextInputDialog dialog = new TextInputDialog(currentReviewText);
            dialog.setTitle("Update Review");
            dialog.setHeaderText("Edit your review");
            dialog.setContentText("Review text:");
            
            dialog.showAndWait().ifPresent(updatedText -> {
                if (!updatedText.trim().isEmpty()) {
                    try {
                        dbHelper3.updateReview(reviewID, updatedText);
                        showAlert("Review updated successfully!", AlertType.INFORMATION);
                    } catch (SQLException ex) {
                        showAlert("Error updating review: " + ex.getMessage(), AlertType.ERROR);
                    }
                } else {
                    showAlert("Review text cannot be empty", AlertType.WARNING);
                }
            });
        } catch (SQLException e) {
            showAlert("Error verifying review ownership: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Deletes an existing review.
     * 
     * @param reviewID The ID of the review to delete
     * @param userName The username of the current user
     */
    public void deleteReview(int reviewID, String userName) {
        try {
            // First verify this review belongs to the current user
            if (!dbHelper3.isReviewOwner(reviewID, userName)) {
                showAlert("You can only delete your own reviews.", AlertType.WARNING);
                return;
            }
            
            Alert confirmation = new Alert(AlertType.CONFIRMATION, 
                    "Are you sure you want to delete this review?", 
                    ButtonType.YES, ButtonType.NO);
            confirmation.showAndWait();
            
            if (confirmation.getResult() == ButtonType.YES) {
                try {
                    dbHelper3.deleteReview(reviewID);
                    showAlert("Review deleted successfully!", AlertType.INFORMATION);
                } catch (SQLException ex) {
                    showAlert("Error deleting review: " + ex.getMessage(), AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            showAlert("Error verifying review ownership: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Opens a window displaying all question reviews.
     * 
     * @param user The current user
     */
    public void viewQuestionReviews(User user) {
        try {
            // Get all question reviews from the database
            List<String[]> reviewList = dbHelper3.getQuestionReviewsWithIDs();
            
            if (reviewList.isEmpty()) {
                showAlert("No question reviews have been posted yet.", AlertType.INFORMATION);
                return;
            }
            
            // Create a window for reviews
            Stage reviewStage = new Stage();
            reviewStage.setTitle("Question Reviews");
            
            showReviewTable(reviewList, reviewStage, user, "question");
            
        } catch (SQLException e) {
            showAlert("Error loading question reviews: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Opens a window displaying all answer reviews.
     * 
     * @param user The current user
     */
    public void viewAnswerReviews(User user) {
        try {
            // Get all answer reviews from the database
            List<String[]> reviewList = dbHelper3.getAnswerReviewsWithIDs();
            
            if (reviewList.isEmpty()) {
                showAlert("No answer reviews have been posted yet.", AlertType.INFORMATION);
                return;
            }
            
            // Create a window for reviews
            Stage reviewStage = new Stage();
            reviewStage.setTitle("Answer Reviews");
            
            showReviewTable(reviewList, reviewStage, user, "answer");
            
        } catch (SQLException e) {
            showAlert("Error loading answer reviews: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Displays a review table with the provided reviews.
     * 
     * @param reviewList The list of reviews to display
     * @param reviewStage The stage to show the table in
     * @param user The current user
     * @param reviewType The type of reviews being displayed ("question" or "answer")
     */
    private void showReviewTable(List<String[]> reviewList, Stage reviewStage, User user, String reviewType) {
        // Create table for reviews
        TableView<String[]> reviewTable = new TableView<>();
        
        // ReviewID column (hidden)
        TableColumn<String[], String> reviewIDColumn = new TableColumn<>("ReviewID");
        reviewIDColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue()[0]));
        reviewIDColumn.setVisible(false); // Hide this column
        
        // ID column
        TableColumn<String[], String> idColumn = new TableColumn<>(reviewType.equals("question") ? "Question ID" : "Answer ID");
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue()[1]));
        idColumn.setPrefWidth(75);
        
        // Content column
        TableColumn<String[], String> contentColumn = new TableColumn<>(reviewType.equals("question") ? "Question" : "Answer");
        contentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue()[2]));
        contentColumn.setPrefWidth(300);
        
        // Review column
        TableColumn<String[], String> reviewColumn = new TableColumn<>("Review");
        reviewColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue()[3]));
        reviewColumn.setPrefWidth(300);
        
        // Reviewer column
        TableColumn<String[], String> reviewerColumn = new TableColumn<>("Reviewer");
        reviewerColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue()[4]));
        reviewerColumn.setPrefWidth(100);
        
        // Date column
        TableColumn<String[], String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue()[5]));
        dateColumn.setPrefWidth(150);
        
        // Actions column for Update/Delete buttons
        TableColumn<String[], Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(150);
        
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button updateBtn = new Button("Update");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, updateBtn, deleteBtn);
            
            {
                pane.setAlignment(Pos.CENTER);
                
                updateBtn.setOnAction(event -> {
                    String[] review = getTableView().getItems().get(getIndex());
                    int reviewID = Integer.parseInt(review[0]);
                    String reviewText = review[3];
                    
                    updateReview(reviewID, reviewText, user.getUserName());
                    
                    // Refresh the table after update
                    try {
                        List<String[]> updatedList = reviewType.equals("question") 
                            ? dbHelper3.getQuestionReviewsWithIDs() 
                            : dbHelper3.getAnswerReviewsWithIDs();
                        getTableView().setItems(FXCollections.observableArrayList(updatedList));
                    } catch (SQLException e) {
                        showAlert("Error refreshing reviews: " + e.getMessage(), AlertType.ERROR);
                    }
                });
                
                deleteBtn.setOnAction(event -> {
                    String[] review = getTableView().getItems().get(getIndex());
                    int reviewID = Integer.parseInt(review[0]);
                    
                    deleteReview(reviewID, user.getUserName());
                    
                    // Refresh the table after delete
                    try {
                        List<String[]> updatedList = reviewType.equals("question") 
                            ? dbHelper3.getQuestionReviewsWithIDs() 
                            : dbHelper3.getAnswerReviewsWithIDs();
                        if (updatedList.isEmpty()) {
                            // Close the window if no reviews left
                            reviewStage.close();
                            showAlert("No more reviews to display.", AlertType.INFORMATION);
                            return;
                        }
                        getTableView().setItems(FXCollections.observableArrayList(updatedList));
                    } catch (SQLException e) {
                        showAlert("Error refreshing reviews: " + e.getMessage(), AlertType.ERROR);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    String[] review = getTableView().getItems().get(getIndex());
                    // Only show buttons if the current user is the reviewer
                    if (review[4].equals(user.getUserName())) {
                        setGraphic(pane);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        reviewTable.getColumns().addAll(reviewIDColumn, idColumn, contentColumn, 
                reviewColumn, reviewerColumn, dateColumn, actionsColumn);
        
        reviewTable.setItems(FXCollections.observableArrayList(reviewList));
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");
        layout.getChildren().add(reviewTable);
        
        Scene scene = new Scene(layout, 1075, 500);
        reviewStage.setScene(scene);
        reviewStage.show();
    }
    
    /**
     * Opens a window displaying reviews for a specific question.
     * 
     * @param questionID The ID of the question
     * @param user The current user
     */
    public void viewReviewsForQuestion(int questionID, User user) {
        try {
            Question question = dbHelper2.getQuestionById(questionID);
            if (question == null) {
                showAlert("Question not found.", AlertType.ERROR);
                return;
            }
            
            List<String[]> reviewList = dbHelper3.getReviewsForQuestionWithIDs(questionID);
            
            if (reviewList.isEmpty()) {
                showAlert("No reviews found for this question.", AlertType.INFORMATION);
                return;
            }
            
            Stage reviewStage = new Stage();
            reviewStage.setTitle("Reviews for Question: " + question.getBodyText());
            
            showReviewTable(reviewList, reviewStage, user, "question");
            
        } catch (SQLException e) {
            showAlert("Error loading reviews: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Opens a window displaying reviews for a specific answer.
     * 
     * @param answer The answer object
     * @param user The current user
     */
    public void viewReviewsForAnswer(Answer answer, User user) {
        try {
            List<String[]> reviewList = dbHelper3.getReviewsForAnswerWithIDs(answer.getAnsID());
            
            if (reviewList.isEmpty()) {
                showAlert("No reviews found for this answer.", AlertType.INFORMATION);
                return;
            }
            
            Stage reviewStage = new Stage();
            reviewStage.setTitle("Reviews for Answer: " + answer.getBodyText());
            
            showReviewTable(reviewList, reviewStage, user, "answer");
            
        } catch (SQLException e) {
            showAlert("Error loading reviews: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Shows an alert dialog with the specified message and type.
     */
    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType, message);
        alert.showAndWait();
    }
}
