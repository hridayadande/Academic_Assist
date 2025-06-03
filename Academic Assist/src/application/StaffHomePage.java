package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.util.Optional;

import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;
import application.Request;

/**
 * This page displays the staff interface for viewing questions, answers, reviews, and feedback.
 * Staff members can view all information but cannot add or modify content.
 */
public class StaffHomePage {
    private DatabaseHelper dbHelper;
    private DatabaseHelper2 dbHelper2;
    private DatabaseHelper3 dbHelper3;
    private User currentUser;

    /**
     * Initializes the StaffHomePage with a database connection.
     */
    public StaffHomePage() {
        this.dbHelper = new DatabaseHelper();
        this.dbHelper2 = new DatabaseHelper2();
        this.dbHelper3 = new DatabaseHelper3();
        try {
            dbHelper.connectToDatabase();
            dbHelper2.connectToDatabase();
            dbHelper3.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the staff page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param user The user object containing staff information.
     */
    public void show(Stage primaryStage, User user) {
        this.currentUser = user;

        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/staff.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true) // Scale image to fit
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));
        
        // Welcome label
        Label userLabel = new Label("Hello, " + user.getfirstName() + "! (Staff)");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Create TableView for questions
        TableView<Question> questionTable = new TableView<>();
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Define table columns
        TableColumn<Question, String> idColumn = new TableColumn<>("Question ID");
        idColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.valueOf(cellData.getValue().getQuestionID())));

        TableColumn<Question, String> bodyColumn = new TableColumn<>("Question");
        bodyColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getBodyText()));

        TableColumn<Question, String> postedByColumn = new TableColumn<>("Posted By");
        postedByColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getPostedBy()));

        TableColumn<Question, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDateCreated().toString()));

        TableColumn<Question, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isResolved() ? "Resolved" : "Unresolved"));

        questionTable.getColumns().addAll(idColumn, bodyColumn, postedByColumn, dateColumn, statusColumn);

        // Initial load of questions
        refreshQuestionTable(questionTable);

        // Buttons for viewing only
        Button viewAnswersButton = new Button("View Answers");
        Button viewReviewsButton = new Button("View Reviews");
        Button viewFeedbackButton = new Button("View Feedback");
        Button flagButton = new Button("Flag");
        Button flaggedActivityButton = new Button("Flagged Activity");
        Button chatButton = new Button("Chat");
        Button requestRestrictButton = new Button("Request to Restrict");
        Button viewAdminActionsButton = new Button("View Admin Actions");
        Button viewClosedRequestsButton = new Button("View Closed Requests");

        // Request to Restrict button
        requestRestrictButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                sendRestrictRequest(selectedQuestion);
            } else {
                showAlert("Please select a question first.", Alert.AlertType.WARNING);
            }
        });

        // View Admin Actions button action
        viewAdminActionsButton.setOnAction(e -> {
            new AdminActionsDialog(user, dbHelper).show();
        });
        
        // View Closed Requests button action
        viewClosedRequestsButton.setOnAction(e -> {
            showClosedRequestsDialog();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");
        buttonBox.getChildren().addAll(viewAnswersButton, viewReviewsButton, viewFeedbackButton, 
                                      flagButton, flaggedActivityButton, chatButton, 
                                      requestRestrictButton, viewAdminActionsButton, viewClosedRequestsButton);

        // View Answers button action
        viewAnswersButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                showAnswersDialog(selectedQuestion);
            } else {
                showAlert("Please select a question first.", Alert.AlertType.WARNING);
            }
        });

        // View Reviews button action
        viewReviewsButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                viewReviewsForQuestion(selectedQuestion);
            } else {
                showAlert("Please select a question to view reviews.", Alert.AlertType.WARNING);
            }
        });

        // View Feedback button action
        viewFeedbackButton.setOnAction(e -> {
            viewAllFeedback();
        });

        // Flag button action
        flagButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                flagContent("Question", selectedQuestion.getQuestionID());
            } else {
                showAlert("Please select a question to flag.", Alert.AlertType.WARNING);
            }
        });
        
        // Flagged Activity button action
        flaggedActivityButton.setOnAction(e -> {
            showFlaggedActivity();
        });

        // Chat button action
        chatButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                openChatWithStudent(selectedQuestion);
            } else {
                showAlert("Please select a question first.", Alert.AlertType.WARNING);
            }
        });

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new SelectRole().show(primaryStage, user, user.getRole()));

        // Add all components to layout
        layout.getChildren().addAll(userLabel, questionTable, buttonBox, backButton);

        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("Staff Page");
    }

    /**
     * Displays a dialog showing answers for a specific question.
     * 
     * @param question The question whose answers are being displayed.
     */
    private void showAnswersDialog(Question question) {
        Stage dialogStage = new Stage();
        VBox dialogLayout = new VBox(10);
        dialogLayout.setStyle("-fx-padding: 20;");

        // Question status label
        Label statusLabel = new Label("Status: " + (question.isResolved() ? "Resolved" : "Unresolved"));
        statusLabel.setStyle("-fx-font-weight: bold;");

        HBox statusBox = new HBox(10);
        statusBox.setStyle("-fx-alignment: center;");
        statusBox.getChildren().addAll(statusLabel);

        // Answer table setup
        TableView<Answer> answerTable = new TableView<>();
        
        // First column: Answer
        TableColumn<Answer, String> answerColumn = new TableColumn<>("Answer");
        answerColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getBodyText()));
        answerColumn.setPrefWidth(300);

        // Second column: Answered By
        TableColumn<Answer, String> answeredByColumn = new TableColumn<>("Answered By");
        answeredByColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getAnsweredBy()));
        answeredByColumn.setPrefWidth(150);

        // Third column: Date
        TableColumn<Answer, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDateCreated().toString()));
        dateColumn.setPrefWidth(150);

        // Fourth column: Accepted Answer
        TableColumn<Answer, String> acceptedColumn = new TableColumn<>("Status");
        acceptedColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getAnsID() == question.getAcceptedAnsID() ? 
                "Accepted" : ""));
        acceptedColumn.setPrefWidth(100);

        answerTable.getColumns().addAll(answerColumn, answeredByColumn, dateColumn, acceptedColumn);
        
        try {
            List<Answer> allAnswers = dbHelper2.getAnswersForQuestion(question.getQuestionID());
            answerTable.setItems(FXCollections.observableArrayList(allAnswers));
        } catch (SQLException ex) {
            showAlert("Error loading answers: " + ex.getMessage(), Alert.AlertType.ERROR);
        }

        // Flag Answer button
        Button flagAnswerButton = new Button("Flag Answer");
        flagAnswerButton.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                flagContent("Answer", selectedAnswer.getAnsID());
            } else {
                showAlert("Please select an answer to flag.", Alert.AlertType.WARNING);
            }
        });

        dialogLayout.getChildren().addAll(statusBox, answerTable, flagAnswerButton);
        dialogStage.setScene(new Scene(dialogLayout, 600, 400));
        dialogStage.setTitle("Answers for Question: " + question.getBodyText());
        dialogStage.show();
    }

    /**
     * Opens a window displaying reviews for a specific question and its answers.
     * 
     * @param question The question whose reviews will be displayed
     */
    private void viewReviewsForQuestion(Question question) {
        try {
            TabPane reviewTabPane = new TabPane();
            reviewTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            Tab questionReviewsTab = new Tab("Question Reviews");
            VBox questionReviewsLayout = new VBox(10);
            questionReviewsLayout.setStyle("-fx-padding: 10;");

            List<String[]> questionReviews = dbHelper3.getReviewsForQuestionWithIDs(question.getQuestionID());

            if (questionReviews.isEmpty()) {
                Label noQuestionsLabel = new Label("No reviews available for this question.");
                questionReviewsLayout.getChildren().add(noQuestionsLabel);
            } else {
                TableView<String[]> questionReviewTable = createReviewTable(questionReviews, "question");
                questionReviewsLayout.getChildren().add(questionReviewTable);
            }

            questionReviewsTab.setContent(questionReviewsLayout);

            Tab answerReviewsTab = new Tab("Answer Reviews");
            VBox answerReviewsLayout = new VBox(10);
            answerReviewsLayout.setStyle("-fx-padding: 10;");

            List<Answer> answers = dbHelper2.getAnswersForQuestion(question.getQuestionID());
            List<String[]> allAnswerReviews = new ArrayList<>();

            for (Answer answer : answers) {
                List<String[]> answerReviews = dbHelper3.getReviewsForAnswerWithIDs(answer.getAnsID());
                allAnswerReviews.addAll(answerReviews);
            }

            if (allAnswerReviews.isEmpty()) {
                Label noAnswersLabel = new Label("No reviews available for answers to this question.");
                answerReviewsLayout.getChildren().add(noAnswersLabel);
            } else {
                TableView<String[]> answerReviewTable = createReviewTable(allAnswerReviews, "answer");
                answerReviewsLayout.getChildren().add(answerReviewTable);
            }

            answerReviewsTab.setContent(answerReviewsLayout);
            reviewTabPane.getTabs().addAll(questionReviewsTab, answerReviewsTab);
            
            Button backButton = new Button("Back");
            backButton.setOnAction(e -> ((Stage) backButton.getScene().getWindow()).close());

            VBox reviewsRootLayout = new VBox(10, reviewTabPane, backButton);
            reviewsRootLayout.setPadding(new Insets(10));
            Scene reviewsScene = new Scene(reviewsRootLayout, 1000, 500);

            Stage reviewsStage = new Stage();
            reviewsStage.setTitle("Reviews for Question: " + question.getBodyText());
            reviewsStage.setScene(reviewsScene);
            reviewsStage.show();

        } catch (SQLException ex) {
            showAlert("Error loading reviews: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Creates a table view for displaying reviews.
     * 
     * @param reviewList The list of reviews to display
     * @param reviewType The type of reviews (question or answer)
     * @return A configured TableView for the reviews
     */
    private TableView<String[]> createReviewTable(List<String[]> reviewList, String reviewType) {
        TableView<String[]> reviewTable = new TableView<>();
        
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
        
        reviewTable.getColumns().addAll(idColumn, contentColumn, reviewColumn, reviewerColumn, dateColumn);
        reviewTable.setItems(FXCollections.observableArrayList(reviewList));
        
        return reviewTable;
    }

    /**
     * View all feedback in the system
     */
    private void viewAllFeedback() {
        try {
            // Collect all feedback for all users
            List<String[]> allFeedback = new ArrayList<>();
            
            // Get a list of all questions
            List<Question> allQuestions = dbHelper2.getAllQuestions();
            
            // For each question, try to get its feedback
            for (Question question : allQuestions) {
                // Get feedback for each user who might have received feedback
                try {
                    // Get the username of the person who posted the question
                    String questionOwner = question.getPostedBy();
                    List<String[]> feedbackForUser = dbHelper3.getFeedbackForUser(questionOwner);
                    
                    // Filter to only include feedback related to this question
                    for (String[] feedback : feedbackForUser) {
                        if (feedback[1].equals(String.valueOf(question.getQuestionID()))) {
                            allFeedback.add(feedback);
                        }
                    }
                } catch (SQLException ex) {
                    // Continue to next question if there's an error
                    continue;
                }
            }

            if (allFeedback.isEmpty()) {
                showAlert("No feedback available in the system.", Alert.AlertType.INFORMATION);
                return;
            }

            // Create a window for feedback
            Stage feedbackStage = new Stage();
            feedbackStage.setTitle("All Feedback");

            // Table
            TableView<String[]> feedbackTable = new TableView<>();

            // Type
            TableColumn<String[], String> typeColumn = new TableColumn<>("Type");
            typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
            typeColumn.setPrefWidth(100);

            // Question ID
            TableColumn<String[], String> questionIDColumn = new TableColumn<>("Question ID");
            questionIDColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
            questionIDColumn.setPrefWidth(100);

            // Question
            TableColumn<String[], String> questionColumn = new TableColumn<>("Question");
            questionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));  
            questionColumn.setPrefWidth(300);

            // Feedback
            TableColumn<String[], String> feedbackColumn = new TableColumn<>("Feedback/Reply");
            feedbackColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[4]));
            feedbackColumn.setPrefWidth(400);

            // From
            TableColumn<String[], String> fromColumn = new TableColumn<>("From");
            fromColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[5]));
            fromColumn.setPrefWidth(100);

            // To
            TableColumn<String[], String> toColumn = new TableColumn<>("To");
            toColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[7] != null ? cellData.getValue()[7] : ""));
            toColumn.setPrefWidth(100);

            // Date-Time
            TableColumn<String[], String> dateTimeColumn = new TableColumn<>("Date-Time");
            dateTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[6]));
            dateTimeColumn.setPrefWidth(200);

            feedbackTable.getColumns().setAll(typeColumn, questionIDColumn, questionColumn, feedbackColumn, 
                                            fromColumn, toColumn, dateTimeColumn);

            feedbackTable.getItems().addAll(allFeedback);

            // Flag Feedback button
            Button flagFeedbackButton = new Button("Flag Feedback");
            flagFeedbackButton.setOnAction(e -> {
                String[] selectedFeedback = feedbackTable.getSelectionModel().getSelectedItem();
                if (selectedFeedback != null) {
                    int feedbackID = Integer.parseInt(selectedFeedback[2]); // Feedback ID is at index 2
                    flagContent("Feedback", feedbackID);
                } else {
                    showAlert("Please select feedback to flag.", Alert.AlertType.WARNING);
                }
            });

            VBox layout = new VBox(10, feedbackTable, flagFeedbackButton);
            layout.setStyle("-fx-padding: 20;");
            Scene scene = new Scene(layout, 1400, 500);

            feedbackStage.setScene(scene);
            feedbackStage.show();

        } catch (SQLException ex) {
            showAlert("Error fetching questions: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Refreshes the question table with the latest data from the database.
     * 
     * @param table The TableView to be refreshed with updated question data.
     */
    private void refreshQuestionTable(TableView<Question> table) {
        try {
            List<Question> questions = dbHelper2.getAllQuestions();
            table.setItems(FXCollections.observableArrayList(questions));
        } catch (SQLException ex) {
            showAlert("Error refreshing questions: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Displays an alert dialog with the specified message and type.
     * 
     * @param message The message to display in the alert.
     * @param alertType The type of alert to show.
     */
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message);
        alert.showAndWait();
    }

    /**
     * Sends a request to instructors to restrict a student from posting.
     * 
     * @param question The question associated with the student to be restricted
     */
    private void sendRestrictRequest(Question question) {
        // Create dialog for entering reason
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Request to Restrict Student");
        dialog.setHeaderText("Request to restrict " + question.getPostedBy() + " from posting");
        
        // Set the button types
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        
        // Create the reason label and field
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextArea reasonField = new TextArea();
        reasonField.setPrefRowCount(5);
        reasonField.setPromptText("Enter reason for restriction request...");
        
        grid.add(new Label("Reason:"), 0, 0);
        grid.add(reasonField, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the reason field by default
        Platform.runLater(() -> reasonField.requestFocus());
        
        // Convert the result to a reason string when the submit button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return reasonField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert("Please provide a reason for the restriction request.", Alert.AlertType.WARNING);
                return;
            }
            
            try {
                // Use the existing feedback system to send the restriction request
                // Get all instructors and send the request to each one
                List<String[]> instructors = dbHelper.getAllInstructors();
                if (instructors.isEmpty()) {
                    showAlert("No instructors found in the system.", Alert.AlertType.WARNING);
                    return;
                }
                
                for (String[] instructor : instructors) {
                    String instructorUsername = instructor[0];
                    // Use the existing feedback system to store the request
                    dbHelper3.insertFeedback(
                        question.getQuestionID(), 
                        instructorUsername,  // sent to instructor
                        currentUser.getUserName(),  // sent by staff
                        "[RESTRICT REQUEST] Student: " + question.getPostedBy() + " - " + reason
                    );
                }
                
                showAlert("Restriction request sent to all instructors.", Alert.AlertType.INFORMATION);
                
            } catch (SQLException ex) {
                showAlert("Error sending restriction request: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Flags content for review.
     * 
     * @param contentType The type of content being flagged (Question, Answer, or Feedback)
     * @param contentID The ID of the content
     */
    private void flagContent(String contentType, int contentID) {
        // Create dialog for entering description
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Flag Content");
        dialog.setHeaderText("Flag " + contentType + " #" + contentID);
        dialog.setContentText("Reason for flagging:");

        dialog.showAndWait().ifPresent(description -> {
            try {
                dbHelper3.flagContent(contentType, contentID, currentUser.getUserName(), description);
                showAlert("This activity has been flagged.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                showAlert("Error flagging content: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Shows a window with all flagged content.
     */
    private void showFlaggedActivity() {
        try {
            List<String[]> flaggedContent = dbHelper3.getAllFlaggedContent();
            
            if (flaggedContent.isEmpty()) {
                showAlert("No flagged content found.", Alert.AlertType.INFORMATION);
                return;
            }
            
            // Create stage for flagged content
            Stage flaggedStage = new Stage();
            flaggedStage.setTitle("Flagged Activity");
            
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            
            // Create table for flagged content
            TableView<String[]> flaggedTable = new TableView<>();
            
            // Define columns
            TableColumn<String[], String> typeColumn = new TableColumn<>("Type");
            typeColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue()[1]));
            
            TableColumn<String[], String> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue()[2]));
            
            TableColumn<String[], String> contentColumn = new TableColumn<>("Content");
            contentColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue()[6]));
            contentColumn.setPrefWidth(300);
            
            TableColumn<String[], String> flaggedByColumn = new TableColumn<>("Flagged By");
            flaggedByColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue()[3]));
            
            TableColumn<String[], String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue()[4]));
            
            TableColumn<String[], String> reasonColumn = new TableColumn<>("Reason");
            reasonColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue()[5]));
            reasonColumn.setPrefWidth(200);
            
            // Action column for resolving flags
            TableColumn<String[], Void> actionColumn = new TableColumn<>("Action");
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button resolveButton = new Button("Resolve");
                
                {
                    resolveButton.setOnAction(event -> {
                        String[] flag = getTableView().getItems().get(getIndex());
                        int flagID = Integer.parseInt(flag[0]);
                        
                        try {
                            dbHelper3.resolveFlaggedContent(flagID);
                            getTableView().getItems().remove(getIndex());
                            
                            if (getTableView().getItems().isEmpty()) {
                                ((Stage) getTableView().getScene().getWindow()).close();
                                showAlert("All flags have been resolved.", Alert.AlertType.INFORMATION);
                            }
                        } catch (SQLException e) {
                            showAlert("Error resolving flag: " + e.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : resolveButton);
                }
            });
            
            flaggedTable.getColumns().addAll(typeColumn, idColumn, contentColumn, 
                                           flaggedByColumn, dateColumn, reasonColumn, actionColumn);
            flaggedTable.setItems(FXCollections.observableArrayList(flaggedContent));
            
            layout.getChildren().add(flaggedTable);
            
            Scene scene = new Scene(layout, 900, 500);
            flaggedStage.setScene(scene);
            flaggedStage.show();
            
        } catch (SQLException e) {
            showAlert("Error loading flagged content: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Opens a chat window with the student who posted the question
     * 
     * @param question The question whose student to chat with
     */
    private void openChatWithStudent(Question question) {
        String studentUsername = question.getPostedBy();
        
        Stage chatWindow = new Stage();
        chatWindow.setTitle("Chat with " + studentUsername + " - Question #" + question.getQuestionID());
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        
        // Show question details at the top
        Label questionLabel = new Label("Question #" + question.getQuestionID() + ": " + question.getBodyText());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-weight: bold;");
        
        // Create a VBox for the chat messages
        VBox chatBox = new VBox(5);
        chatBox.setPadding(new Insets(10));
        chatBox.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        // Create a ScrollPane to contain the messages
        ScrollPane scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(400);
        
        // Create input area
        TextField messageField = new TextField();
        messageField.setPromptText("Type your message here...");
        Button sendButton = new Button("Send");
        
        HBox inputArea = new HBox(10, messageField, sendButton);
        inputArea.setAlignment(Pos.CENTER);
        messageField.setPrefWidth(500);
        
        // Status label for errors
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: blue;");
        
        // Load existing messages
        try {
            List<String> messages = dbHelper3.getChatMessagesForQuestion(studentUsername, question.getQuestionID());
            if (messages.isEmpty()) {
                statusLabel.setText("No previous messages. Send a message to start the conversation.");
            } else {
                displayChatMessages(chatBox, messages, studentUsername);
            }
        } catch (SQLException ex) {
            statusLabel.setText("Chat initialized. Send a message to start the conversation.");
        }
        
        // Set up send action
        sendButton.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                try {
                    // Always use "Staff" as role for consistency
                    dbHelper3.insertChatMessage("Staff", currentUser.getUserName(), question.getQuestionID(), message);
                    
                    // Refresh chat display
                    messageField.clear();
                    chatBox.getChildren().clear();
                    List<String> updatedMessages = dbHelper3.getChatMessagesForQuestion(studentUsername, question.getQuestionID());
                    displayChatMessages(chatBox, updatedMessages, studentUsername);
                    statusLabel.setText(""); // Clear status message after successful send
                    
                    // Scroll to bottom after new message
                    scrollPane.setVvalue(1.0);
                } catch (SQLException ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        // Set up keyboard shortcut for sending (Enter key)
        messageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });
        
        // Add everything to the layout
        layout.getChildren().addAll(questionLabel, scrollPane, statusLabel, inputArea);
        
        Scene scene = new Scene(layout, 600, 500);
        chatWindow.setScene(scene);
        chatWindow.show();
    }
    
    /**
     * Displays chat messages in the given VBox
     * @param chatBox The VBox to display messages in
     * @param messages The list of messages to display
     * @param studentUsername The username of the student in the conversation
     */
    private void displayChatMessages(VBox chatBox, List<String> messages, String studentUsername) {
        if (messages.isEmpty()) {
            chatBox.getChildren().add(new Label("No messages yet. Send a message to start the conversation."));
            return;
        }
        
        for (String messageData : messages) {
            // Parse the message data from format: "Role: Message (Timestamp)"
            String[] parts = messageData.split(": ", 2);
            if (parts.length < 2) continue;
            
            String sender = parts[0];
            
            // Split the message and timestamp
            int timestampStart = parts[1].lastIndexOf(" (");
            String message = parts[1];
            String timestamp = "";
            
            if (timestampStart > 0) {
                message = parts[1].substring(0, timestampStart);
                timestamp = parts[1].substring(timestampStart + 2, parts[1].length() - 1);
            }
            
            // Create a message bubble with appropriate styling
            VBox messageBubble = new VBox(2);
            messageBubble.setPadding(new Insets(8));
            messageBubble.setMaxWidth(400);
            
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            
            Label metaLabel = new Label(sender + " - " + timestamp);
            metaLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666;");
            
            messageBubble.getChildren().addAll(messageLabel, metaLabel);
            
            HBox container = new HBox();
            if (sender.equals("Staff") || sender.contains(currentUser.getUserName())) {
                // Current user's message - right aligned
                messageBubble.setStyle(messageBubble.getStyle() + "-fx-background-color: #dcf8c6; -fx-background-radius: 10px;");
                container.setAlignment(Pos.CENTER_RIGHT);
            } else {
                // Student's message - left aligned
                messageBubble.setStyle(messageBubble.getStyle() + "-fx-background-color: #ffffff; -fx-background-radius: 10px;");
                container.setAlignment(Pos.CENTER_LEFT);
            }
            
            container.getChildren().add(messageBubble);
            chatBox.getChildren().add(container);
        }
    }

    /**
     * Shows a dialog displaying closed admin requests related to the current user.
     */
    private void showClosedRequestsDialog() {
        try {
            ObservableList<Request> closedRequests = dbHelper.getClosedAdminRequests();
            // Filter for requests related to the current user
            ObservableList<Request> userRequests = closedRequests.filtered(
                request -> request.getUsername().equals(currentUser.getUserName())
            );
            
            if (userRequests.isEmpty()) {
                showAlert("No closed admin requests found for your account.", Alert.AlertType.INFORMATION);
                return;
            }
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Closed Admin Requests");
            
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));
            
            Label titleLabel = new Label("Your Closed Admin Requests");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            
            TableView<Request> requestsTable = new TableView<>();
            
            // Show description in the Reason column to match View Admin Requests
            TableColumn<Request, String> reasonColumn = new TableColumn<>("Reason");
            reasonColumn.setCellValueFactory(cell -> 
                new SimpleStringProperty(cell.getValue().getDescription()));
            reasonColumn.setPrefWidth(300);
            
            TableColumn<Request, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(cell -> 
                new SimpleStringProperty(cell.getValue().getDate()));
            dateColumn.setPrefWidth(150);
            
            requestsTable.getColumns().addAll(reasonColumn, dateColumn);
            requestsTable.setItems(userRequests);
            
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> dialogStage.close());
            
            layout.getChildren().addAll(
                titleLabel,
                requestsTable, 
                closeButton
            );
            
            Scene scene = new Scene(layout, 600, 400);
            dialogStage.setScene(scene);
            dialogStage.show();
            
        } catch (SQLException e) {
            showAlert("Error loading closed requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}