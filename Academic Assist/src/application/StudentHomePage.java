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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.control.SplitPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;

/**
 * This page handles the student interface for viewing and managing questions and answers.
 * Students can ask questions, provide answers, and track their interactions.
 */
public class StudentHomePage {
    private DatabaseHelper dbHelper;
    private DatabaseHelper2 dbHelper2;
    private DatabaseHelper3 dbHelper3;
    private User currentUser;

    /**
     * Initializes the StudentHomePage with a database connection.
     */
    public StudentHomePage() {
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
     * Displays the student page in the provided primary stage.
     * 
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param user The user object containing student information.
     */
    public void show(Stage primaryStage, User user) {
    	this.currentUser = user;
        boolean isRestricted = user.getRole().contains("Restricted");
        
        // Debug logging
        System.out.println("Student HomePage - User: " + user.getUserName());
        System.out.println("Role: " + user.getRole());
        System.out.println("Is Restricted: " + isRestricted);

        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        
        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/student.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true) // Scale image to fit
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));
        
        // Add restriction warning for restricted accounts
        if (isRestricted) {
            Label restrictedLabel = new Label("RESTRICTED ACCOUNT");
            restrictedLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red; -fx-background-color: #FFEEEE; -fx-padding: 10px; -fx-border-color: red; -fx-border-width: 1px;");
            restrictedLabel.setAlignment(Pos.CENTER);
            restrictedLabel.setMaxWidth(Double.MAX_VALUE);
            layout.getChildren().add(restrictedLabel);
        }
        
        // Welcome label
        Label userLabel = new Label("Hello, " + user.getfirstName() + "! (Student)");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Create TableView for questions
        TableView<Question> questionTable = new TableView<>();
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Create TableView for answers
        TableView<Answer> answerTable = new TableView<>();
        answerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        TableColumn<Question, String> unreadColumn = new TableColumn<>("Unread Answers");
        unreadColumn.setCellValueFactory(cellData -> {
            try {
                // Get total number of answers for this question
                List<Answer> answers = dbHelper2.getAnswersForQuestion(cellData.getValue().getQuestionID());
                int unreadCount = cellData.getValue().getNewMessagesCount();
                return new SimpleStringProperty(unreadCount > 0 ? String.valueOf(unreadCount) : "");
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("");
            }
        });

        questionTable.getColumns().addAll(idColumn, bodyColumn, postedByColumn, dateColumn, 
                                        statusColumn, unreadColumn);

        // Search bar with filter options
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(15, 10, 15, 10));
        searchBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 5px;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search questions...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll(
            "All Questions", 
            "Questions with Answers", 
            "Questions without Answers", 
            "Questions by Reviewer"
        );
        filterComboBox.setValue("All Questions");
        filterComboBox.setStyle("-fx-font-size: 13px;");
        
        ComboBox<String> reviewerComboBox = new ComboBox<>();
        reviewerComboBox.setVisible(false);
        reviewerComboBox.setPromptText("Select reviewer");
        reviewerComboBox.setStyle("-fx-font-size: 13px;");
        
        // Create a clear button
        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-font-size: 13px; -fx-background-color: #f0f0f0;");
        clearButton.setOnAction(e -> {
            searchField.clear();
            filterComboBox.setValue("All Questions");
            reviewerComboBox.setVisible(false);
            refreshQuestionTable(questionTable);
        });
        
        // Load reviewers for the reviewer filter
        try {
            reviewerComboBox.getItems().addAll(dbHelper3.getAllReviewers());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        Button searchButton = new Button("\uD83D\uDD0D"); // Unicode for magnifying glass
        searchButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4285f4; -fx-text-fill: white;");
        searchButton.setOnAction(e -> {
            String filterValue = null;
            String filterType = mapFilterType(filterComboBox.getValue());
            
            if ("Reviewer".equals(filterType)) {
                filterValue = reviewerComboBox.getValue();
            }
            applySearchFilter(searchField.getText(), filterType, filterValue, questionTable);
        });
        
        // Show/hide reviewer combobox based on filter selection
        filterComboBox.setOnAction(e -> {
            boolean showReviewerFilter = "Questions by Reviewer".equals(filterComboBox.getValue());
            reviewerComboBox.setVisible(showReviewerFilter);
            
            String filterType = mapFilterType(filterComboBox.getValue());
            
            if (!showReviewerFilter) {
                applySearchFilter(searchField.getText(), filterType, null, questionTable);
            } else if (reviewerComboBox.getValue() != null) {
                applySearchFilter(searchField.getText(), filterType, reviewerComboBox.getValue(), questionTable);
            }
        });
        
        // Apply filter when reviewer is selected
        reviewerComboBox.setOnAction(e -> {
            if (reviewerComboBox.isVisible() && reviewerComboBox.getValue() != null) {
                applySearchFilter(searchField.getText(), filterComboBox.getValue(), reviewerComboBox.getValue(), questionTable);
            }
        });
        
        // Apply filter when text is entered
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String filterValue = null;
            String filterType = mapFilterType(filterComboBox.getValue());
            
            if ("Reviewer".equals(filterType)) {
                filterValue = reviewerComboBox.getValue();
            }
            applySearchFilter(newValue, filterType, filterValue, questionTable);
        });
        
        // Add Enter key event handler to search field
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String filterValue = null;
                String filterType = mapFilterType(filterComboBox.getValue());
                
                if ("Reviewer".equals(filterType)) {
                    filterValue = reviewerComboBox.getValue();
                }
                applySearchFilter(searchField.getText(), filterType, filterValue, questionTable);
            }
        });
        
        // Add search components to search box
        searchBox.getChildren().addAll(searchField, searchButton, filterLabel, filterComboBox, reviewerComboBox, clearButton);

        // Initial load of questions
        refreshQuestionTable(questionTable);

        // Buttons for question management
        Button askQuestionButton = new Button("Ask Question");
        Button viewAnswersButton = new Button("Answer");
        Button updateQuestionButton = new Button("Update Question");
        Button deleteButton = new Button("Delete");
        Button feedbackButton = new Button("Feedback");
        Button inboxButton = new Button("Inbox");
        Button replyChainButton = new Button("Reply");
        Button viewReviewsButton = new Button("View Reviews");
        Button requestReviewerButton = new Button("Request Reviewer Role"); 
        Button flaggedActivityButton = new Button("Flagged Activity");

        // Disable buttons for restricted accounts except for permitted ones
        if (isRestricted) {
            askQuestionButton.setDisable(true);
            updateQuestionButton.setDisable(true);
            deleteButton.setDisable(true);
            feedbackButton.setDisable(true);
            replyChainButton.setDisable(true);
            requestReviewerButton.setDisable(true);
            
            // Optional: Add tooltip explaining the restriction
            Tooltip restrictedTooltip = new Tooltip("Your account is currently restricted");
            Tooltip.install(askQuestionButton, restrictedTooltip);
            Tooltip.install(updateQuestionButton, restrictedTooltip);
            Tooltip.install(deleteButton, restrictedTooltip);
            Tooltip.install(feedbackButton, restrictedTooltip);
            Tooltip.install(replyChainButton, restrictedTooltip);
            Tooltip.install(requestReviewerButton, restrictedTooltip);
            
            System.out.println("Buttons disabled for restricted user");
            System.out.println("Ask Question button disabled: " + askQuestionButton.isDisable());
            System.out.println("Update Question button disabled: " + updateQuestionButton.isDisable());
        }
        
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");
        buttonBox.getChildren().addAll(askQuestionButton, viewAnswersButton, updateQuestionButton, deleteButton, feedbackButton, inboxButton, replyChainButton, viewReviewsButton, requestReviewerButton, flaggedActivityButton);

        // Ask Question button action
        askQuestionButton.setOnAction(e -> {
            showAskQuestionDialog(questionTable);
        });

        requestReviewerButton.setOnAction(e -> {
            try {
                // Get all instructors from the database
                List<String[]> instructors = dbHelper.getAllInstructors();
                
                if (instructors.isEmpty()) {
                    showAlert("No instructors found in the system.", Alert.AlertType.WARNING);
                    return;
                }

                // Create a dialog with a dropdown menu
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Request Reviewer Role");
                dialog.setHeaderText("Select an instructor and provide your request message");

                // Create the dropdown menu
                ComboBox<String> instructorComboBox = new ComboBox<>();
                for (String[] instructor : instructors) {
                    instructorComboBox.getItems().add(instructor[0] + " (" + instructor[1] + " " + instructor[2] + ")");
                }
                instructorComboBox.setPromptText("Select an instructor");

                // Create the text area for the request message
                TextArea requestMessageArea = new TextArea();
                requestMessageArea.setPromptText("Enter your request message");
                requestMessageArea.setPrefRowCount(3);

                // Add the components to the dialog
                VBox content = new VBox(10);
                content.getChildren().addAll(instructorComboBox, requestMessageArea);
                dialog.getDialogPane().setContent(content);

                // Add buttons to the dialog
                ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

                // Set the result converter
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == submitButtonType) {
                        String selectedInstructor = instructorComboBox.getValue();
                        String requestMessage = requestMessageArea.getText().trim();

                        if (selectedInstructor == null || selectedInstructor.isEmpty()) {
                            showAlert("Please select an instructor.", Alert.AlertType.WARNING);
                            return null;
                        }

                        if (requestMessage.isEmpty()) {
                            showAlert("Please enter a request message.", Alert.AlertType.WARNING);
                            return null;
                        }

                        // Extract the instructor username from the selected value
                        String instructorUsername = selectedInstructor.split(" ")[0];

                        // Create and submit the request
                        try {
                            ReviewerRequest newRequest = new ReviewerRequest(
                                currentUser.getUserName(),
                                instructorUsername,
                                requestMessage,
                                LocalDateTime.now()
                            );
                            newRequest.setRequestID(UUID.randomUUID().toString());
                            dbHelper.insertReviewerRequest(newRequest);
                            showAlert("Request sent to " + instructorUsername + "!", Alert.AlertType.INFORMATION);
                        } catch (SQLException ex) {
                            showAlert("Error submitting request: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                    return null;
                });

                dialog.showAndWait();

            } catch (SQLException ex) {
                showAlert("Error loading instructors: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        
        // View Answers button action
        viewAnswersButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                try {
                    // Reset unread count when viewing answers
                    if (selectedQuestion.getNewMessagesCount() > 0) {
                        selectedQuestion.setNewMessagesCount(0);
                        dbHelper2.updateQuestion(selectedQuestion);
                        refreshQuestionTable(questionTable);
                    }
                    showAnswersDialog(selectedQuestion, user, questionTable);
                } catch (SQLException ex) {
                    showAlert("Error updating unread count: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Please select a question first.", Alert.AlertType.WARNING);
            }
        });

        // Update Question button action
        updateQuestionButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                if (selectedQuestion.getPostedBy().equals(user.getUserName())) {
                    TextInputDialog updateDialog = new TextInputDialog(selectedQuestion.getBodyText());
                    updateDialog.setTitle("Update Question");
                    updateDialog.setHeaderText("Update your question:");
                    updateDialog.setContentText("Question:");

                    updateDialog.showAndWait().ifPresent(updatedText -> {
                        try {
                            selectedQuestion.setBodyText(updatedText);
                            dbHelper2.updateQuestion(selectedQuestion);
                            refreshQuestionTable(questionTable);
                        } catch (SQLException ex) {
                            showAlert("Error updating question: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                } else {
                    showAlert("You can only update your own questions.", Alert.AlertType.WARNING);
                }
            } else {
                showAlert("Please select a question to update.", Alert.AlertType.WARNING);
            }
        });

        // Delete button action
        deleteButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                if (selectedQuestion.getPostedBy().equals(user.getUserName())) {
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                            "Are you sure you want to delete this question?",
                            ButtonType.YES, ButtonType.NO);
                    confirmation.showAndWait();
                    
                    if (confirmation.getResult() == ButtonType.YES) {
                        try {
                            dbHelper2.deleteQuestion(selectedQuestion.getQuestionID());
                            refreshQuestionTable(questionTable);
                        } catch (SQLException ex) {
                            showAlert("Error deleting question: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                } else {
                    showAlert("You can only delete your own questions.", Alert.AlertType.WARNING);
                }
            } else {
                showAlert("Please select a question to delete.", Alert.AlertType.WARNING);
            }
        });

        feedbackButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            
            if (selectedQuestion != null) {
                // Get the username of the person who posted the selected question
                String questionOwner = selectedQuestion.getPostedBy();

                // Prevent the question owner from giving feedback to themselves
                if (user.getUserName().equals(questionOwner)) {
                    showAlert("You cannot give feedback on your own question.", Alert.AlertType.WARNING);
                    return;
                }

                // Open a text input dialog for feedback
                TextInputDialog feedbackDialog = new TextInputDialog();
                feedbackDialog.setTitle("Give Feedback");
                feedbackDialog.setHeaderText("Provide feedback for the question:\n" + selectedQuestion.getBodyText());
                feedbackDialog.setContentText("Enter your feedback:");

                feedbackDialog.showAndWait().ifPresent(feedbackText -> {
                    if (!feedbackText.trim().isEmpty()) {
                        try {
                            // Insert feedback into the database with question ID
                            dbHelper3.insertFeedback(selectedQuestion.getQuestionID(), questionOwner, user.getUserName(), feedbackText);
                            showAlert("Feedback sent successfully!", Alert.AlertType.INFORMATION);
                        } catch (SQLException ex) {
                            showAlert("Error saving feedback: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Feedback cannot be empty!", Alert.AlertType.WARNING);
                    }
                });

            } else {
                showAlert("Please select a question first to give feedback.", Alert.AlertType.WARNING);
            }
        });

        // Button to open the Inbox
        inboxButton.setOnAction(e -> {
            try {
                showInboxWithTabs();
            } catch (SQLException ex) {
                showAlert("Error opening inbox: " + ex.getMessage(), Alert.AlertType.ERROR);
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
        

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new SelectRole().show(primaryStage, user, user.getRole()));
        
        replyChainButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                openChatWithReviewerWindow(selectedQuestion); // Use the new table-based chat window
            } else {
                showAlert("Please select a question to open chat.", Alert.AlertType.WARNING);
            }
        });
        

        // Flagged Activity button action
        flaggedActivityButton.setOnAction(e -> {
            showFlaggedActivity();
        });

        // Add all components to layout
        layout.getChildren().addAll(userLabel, searchBox, questionTable, buttonBox, backButton);

        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("Student Page");
    }

    /**
     * Displays a dialog allowing the student to ask a new question.
     * @param questionTable The question table to refresh after adding a new question
     */
    private void showAskQuestionDialog(TableView<Question> questionTable) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Ask a Question");
        dialog.setHeaderText("Enter your question");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 150, 10, 10));

        TextArea questionArea = new TextArea();
        questionArea.setPromptText("Type your question here...");
        questionArea.setPrefHeight(100);

        content.getChildren().addAll(new Label("Question:"), questionArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                String questionText = questionArea.getText().trim();
                if (!questionText.isEmpty()) {
                    // Using the 4-parameter constructor instead of the undefined 7-parameter constructor
                    Question question = new Question(
                            0, // Temporary ID, will be set by database
                            questionText,
                            currentUser.getUserName(),
                            new Date()
                    );
                    // Set additional properties using setters
                    question.setResolved(false);
                    question.setAcceptedAnsID(0);
                    question.setNewMessagesCount(0);
                    return question;
                }
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();

        result.ifPresent(question -> {
            try {
                // Use DatabaseHelper3 to insert the question
                dbHelper2.insertQuestion(question);
                
                // Show success alert
                showAlert("Your question has been posted successfully!", Alert.AlertType.INFORMATION);
                
                // Refresh the question table
                refreshQuestionTable(questionTable);
            } catch (SQLException e) {
                showAlert("Error adding question: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Displays a dialog showing answers for a specific question.
     * 
     * @param question The question whose answers are being displayed.
     * @param user The current user viewing the answers.
     * @param questionTable The main question table to refresh after updates.
     */
    private void showAnswersDialog(Question question, User user, TableView<Question> questionTable) {
        Stage dialogStage = new Stage();
        VBox dialogLayout = new VBox(10);
        dialogLayout.setStyle("-fx-padding: 20;");

        // Question details at the top
        Label questionLabel = new Label("Question: " + question.getBodyText());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-weight: bold;");

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

        // Add Answer button
        Button addAnswerButton = new Button("Add Answer");
        addAnswerButton.setOnAction(e -> {
            if (user.getRole().contains("Restricted")) {
                showAlert("Your account is restricted from posting answers.", Alert.AlertType.WARNING);
                return;
            }
            
            TextInputDialog answerDialog = new TextInputDialog();
            answerDialog.setTitle("Add Answer");
            answerDialog.setHeaderText("Enter your answer to the question:");
            answerDialog.setContentText("Answer:");

            answerDialog.showAndWait().ifPresent(answerText -> {
                if (!answerText.trim().isEmpty()) {
                    try {
                        // Create a new answer object
                    Answer newAnswer = new Answer(
                                0, // Temporary ID, will be set by database
                        question.getQuestionID(),
                        answerText,
                        user.getUserName(),
                        new Date()
                    );

                        // Insert the answer using DatabaseHelper3
                    dbHelper2.insertAnswer(newAnswer);

                        // Show success alert
                        showAlert("Your answer has been posted!", Alert.AlertType.INFORMATION);
                        
                        // Refresh the answer table
                        List<Answer> updatedAnswers = dbHelper2.getAnswersForQuestion(question.getQuestionID());
                        answerTable.setItems(FXCollections.observableArrayList(updatedAnswers));

                } catch (SQLException ex) {
                        showAlert("Error adding answer: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Answer cannot be empty!", Alert.AlertType.WARNING);
                }
            });
        });

        // Accept Answer button (only visible if the user is the question owner and the question is not resolved)
        Button acceptAnswerButton = new Button("Accept Answer");
        acceptAnswerButton.setVisible(question.getPostedBy().equals(user.getUserName()) && !question.isResolved());
        acceptAnswerButton.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                try {
                    // Update the question to mark it as resolved and set the accepted answer
                    dbHelper2.acceptAnswer(question.getQuestionID(), selectedAnswer.getAnsID());
                    
                    // Update the UI
                    showAlert("Answer accepted as solution!", Alert.AlertType.INFORMATION);
                    statusLabel.setText("Status: Resolved");
                    acceptAnswerButton.setVisible(false);
                    
                    // Refresh the tables
                    List<Answer> updatedAnswers = dbHelper2.getAnswersForQuestion(question.getQuestionID());
                    answerTable.setItems(FXCollections.observableArrayList(updatedAnswers));
                    refreshQuestionTable(questionTable);
                    
                        } catch (SQLException ex) {
                    showAlert("Error accepting answer: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                } else {
                showAlert("Please select an answer to accept.", Alert.AlertType.WARNING);
            }
        });

        // Button to send feedback about an answer
        Button feedbackButton = new Button("Send Feedback");
        feedbackButton.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                TextInputDialog feedbackDialog = new TextInputDialog();
                feedbackDialog.setTitle("Send Feedback");
                feedbackDialog.setHeaderText("Send feedback to " + selectedAnswer.getAnsweredBy());
                feedbackDialog.setContentText("Feedback:");
                
                feedbackDialog.showAndWait().ifPresent(feedbackText -> {
                    if (!feedbackText.trim().isEmpty()) {
                        try {
                            // Insert feedback using DatabaseHelper3
                            dbHelper3.insertFeedback(
                                    question.getQuestionID(),
                                    selectedAnswer.getAnsweredBy(),  // Send to the answer author
                                    user.getUserName(),  // From the current user
                                    feedbackText
                            );
                            
                            showAlert("Feedback sent successfully!", Alert.AlertType.INFORMATION);
                        } catch (SQLException ex) {
                            showAlert("Error sending feedback: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Feedback cannot be empty!", Alert.AlertType.WARNING);
                    }
                });
                } else {
                showAlert("Please select an answer to send feedback on.", Alert.AlertType.WARNING);
            }
        });

        // Button to chat with a reviewer
        Button chatWithReviewerButton = new Button("Chat with Reviewer");
        chatWithReviewerButton.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null && selectedAnswer.getAnsweredBy().contains("Reviewer")) {
                openChatWithReviewerWindow(question);
            } else {
                showAlert("Please select an answer from a reviewer to chat with.", Alert.AlertType.WARNING);
            }
        });

        // Flag Answer button
        Button flagAnswerButton = new Button("Flag Answer");
        flagAnswerButton.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                TextInputDialog flagDialog = new TextInputDialog();
                flagDialog.setTitle("Flag Answer");
                flagDialog.setHeaderText("Flag answer from " + selectedAnswer.getAnsweredBy());
                flagDialog.setContentText("Reason for flagging:");
                
                flagDialog.showAndWait().ifPresent(reason -> {
                    if (!reason.trim().isEmpty()) {
                        try {
                            dbHelper3.flagContent("Answer", selectedAnswer.getAnsID(), user.getUserName(), reason);
                            showAlert("Answer has been flagged for review.", Alert.AlertType.INFORMATION);
                } catch (SQLException ex) {
                            showAlert("Error flagging answer: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Please provide a reason for flagging.", Alert.AlertType.WARNING);
                    }
                });
            } else {
                showAlert("Please select an answer to flag.", Alert.AlertType.WARNING);
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");
        buttonBox.getChildren().addAll(addAnswerButton, acceptAnswerButton, feedbackButton, chatWithReviewerButton, flagAnswerButton);

        dialogLayout.getChildren().addAll(questionLabel, statusBox, answerTable, buttonBox);
        dialogStage.setScene(new Scene(dialogLayout, 750, 400));
        dialogStage.setTitle("Answers for Question #" + question.getQuestionID());
        dialogStage.show();
    }

    private String chatHistory = ""; // Store chat messages as one string
    public void openChatWithReviewerWindow(Question selectedQuestion) {
        Stage chatWindow = new Stage();
        chatWindow.setTitle("Chat for Question #" + selectedQuestion.getQuestionID());

        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));

        // Add question details at the top
        Label questionLabel = new Label("Question: " + selectedQuestion.getBodyText());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-weight: bold;");

        // Create a table for chat messages
        TableView<String[]> chatTable = new TableView<>();
        chatTable.setPrefHeight(250);

        TableColumn<String[], String> senderColumn = new TableColumn<>("From");
        senderColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        senderColumn.setPrefWidth(100);

        TableColumn<String[], String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        messageColumn.setPrefWidth(300);

        TableColumn<String[], String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        timeColumn.setPrefWidth(150);

        chatTable.getColumns().addAll(senderColumn, messageColumn, timeColumn);

        // Create a label to show status or errors
        Label statusLabel = new Label("");
        
        // Load existing messages
        try {
            List<String> messages = dbHelper3.getChatMessagesForQuestion(currentUser.getUserName(), selectedQuestion.getQuestionID());
            List<String[]> formattedMessages = new ArrayList<>();
            
            for (String message : messages) {
                String[] parts = message.split(": ", 2);
                if (parts.length == 2) {
                    String[] messageParts = parts[1].split(" \\(", 2);
                    if (messageParts.length == 2) {
                        String time = messageParts[1].substring(0, messageParts[1].length() - 1);
                        formattedMessages.add(new String[]{parts[0], messageParts[0], time});
                    }
                }
            }
            
            chatTable.setItems(FXCollections.observableArrayList(formattedMessages));
        } catch (SQLException e) {
            statusLabel.setText("Chat initialized. Send a message to start the conversation.");
            statusLabel.setStyle("-fx-text-fill: blue;");
        }

        // Input area
        HBox inputBox = new HBox(5);
        inputBox.setAlignment(Pos.CENTER);

        TextField messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setPrefWidth(400);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                try {
                    dbHelper3.insertChatMessage("Student", currentUser.getUserName(), selectedQuestion.getQuestionID(), message);
                    
                    // Add new message to table
                    String[] newMessage = new String[]{"Student", message, new Date().toString()};
                    chatTable.getItems().add(newMessage);
                    
                    // Clear input field
                    messageField.clear();
                    
                    // Scroll to bottom
                    chatTable.scrollTo(chatTable.getItems().size() - 1);
                    
                    // Clear any status messages
                    statusLabel.setText("");
                    
                } catch (SQLException ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });

        inputBox.getChildren().addAll(messageField, sendButton);
        
        // Add all components to the layout
        chatBox.getChildren().addAll(questionLabel, chatTable, statusLabel, inputBox);

        Scene chatScene = new Scene(chatBox, 600, 400);
        chatWindow.setScene(chatScene);
        chatWindow.show();
    }


    /**
     * Refreshes the question table with the latest data from the database.
     * 
     * @param table The TableView to be refreshed with updated question data.
     */
    private void refreshQuestionTable(TableView<Question> table) {
        try {
            // Use the new searchQuestions method with no filters
            List<Question> questions = dbHelper3.searchQuestions("", "All", null);
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
        
        // Add View Profile column
        TableColumn<String[], Void> profileColumn = new TableColumn<>("Profile");
        profileColumn.setPrefWidth(100);
        profileColumn.setCellFactory(param -> new TableCell<>() {
            private final Button profileButton = new Button("View Profile");

            {
                profileButton.setOnAction(event -> {
                    String[] review = getTableView().getItems().get(getIndex());
                    String reviewerName = review[4];
                    
                    showReviewerProfile(reviewerName);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(profileButton);
                }
            }
        });
        
        // Add Rate Review column
        TableColumn<String[], Void> rateColumn = new TableColumn<>("Rate Review");
        rateColumn.setPrefWidth(100);
        rateColumn.setCellFactory(param -> new TableCell<>() {
            private final Button rateButton = new Button("Rate");

            {
                rateButton.setOnAction(event -> {
                    String[] review = getTableView().getItems().get(getIndex());
                    String reviewerName = review[4];
                    
                    // Create rating dialog
                    Dialog<Integer> dialog = new Dialog<>();
                    dialog.setTitle("Rate Review");
                    dialog.setHeaderText("Rate " + reviewerName + "'s review\nScale: 1-5 (5 being the best)");

                    // Create rating options
                    ComboBox<Integer> ratingBox = new ComboBox<>();
                    ratingBox.getItems().addAll(1, 2, 3, 4, 5);
                    ratingBox.setPromptText("Select rating");

                    // Create feedback text area
                    TextArea feedbackArea = new TextArea();
                    feedbackArea.setPromptText("Optional feedback for the reviewer");
                    feedbackArea.setPrefRowCount(3);

                    // Layout
                    VBox content = new VBox(10);
                    content.getChildren().addAll(
                        new Label("Rating:"), 
                        ratingBox,
                        new Label("Feedback (optional):"),
                        feedbackArea
                    );
                    dialog.getDialogPane().setContent(content);

                    // Add buttons
                    ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

                    // Convert result
                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == submitButtonType) {
                            return ratingBox.getValue();
                        }
                        return null;
                    });

                    // Handle the result
                    dialog.showAndWait().ifPresent(rating -> {
                        if (rating != null) {
                            try {
                                // Save the rating
                                dbHelper3.setReviewerWeight(currentUser.getUserName(), reviewerName, rating);

                                // If feedback was provided, save it too
                                String feedback = feedbackArea.getText().trim();
                                if (!feedback.isEmpty()) {
                                    int reviewID = Integer.parseInt(review[0]); // Get review ID
                                    int targetID = Integer.parseInt(review[1]); // Get target ID
                                    boolean isAnswer = reviewType.equals("answer");
                                    
                                    dbHelper3.insertReviewFeedback(
                                        reviewID,
                                        targetID,
                                        isAnswer,
                                        currentUser.getUserName(),
                                        reviewerName,
                                        feedback
                                    );
                                }

                                showAlert("Rating submitted successfully!", Alert.AlertType.INFORMATION);
                            } catch (SQLException ex) {
                                showAlert("Error saving rating: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    String[] review = getTableView().getItems().get(getIndex());
                    // Only show rate button if the review is not by the current user
                    if (!review[4].equals(currentUser.getUserName())) {
                        setGraphic(rateButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        reviewTable.getColumns().addAll(idColumn, contentColumn, reviewColumn, 
                                      reviewerColumn, dateColumn, profileColumn, rateColumn);
        reviewTable.setItems(FXCollections.observableArrayList(reviewList));
        
        return reviewTable;
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
            
            // Action column for resolving flags (if user has appropriate role)
            if (currentUser.getRole().contains("Staff") || currentUser.getRole().contains("Instructor")) {
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
            } else {
            flaggedTable.getColumns().addAll(typeColumn, idColumn, contentColumn, 
                                           flaggedByColumn, dateColumn, reasonColumn);
            }
            
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
     * Displays the inbox with tabs for different message types
     */
    private void showInboxWithTabs() throws SQLException {
        // Get feedback messages for this student
        List<String[]> feedbackMessages = dbHelper3.getFeedbackForUser(currentUser.getUserName());
        
        // Get unread chats with reviewers and/or staff
        List<String[]> generalChatMessages = dbHelper3.getGeneralChatMessages(currentUser.getUserName());
        
        // Check if there are any messages
        if (feedbackMessages.isEmpty() && generalChatMessages.isEmpty()) {
            showAlert("Your inbox is empty.", Alert.AlertType.INFORMATION);
            return;
        }
        
        Stage inboxStage = new Stage();
        inboxStage.setTitle("Student Inbox");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Feedback tab
        if (!feedbackMessages.isEmpty()) {
        Tab feedbackTab = new Tab("Feedback");
        VBox feedbackLayout = new VBox(10);
        feedbackLayout.setPadding(new Insets(10));
        
            // Table
            TableView<String[]> inboxTable = new TableView<>();

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
            fromColumn.setPrefWidth(200);

            // Date-Time
            TableColumn<String[], String> dateTimeColumn = new TableColumn<>("Date-Time");
            dateTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[6]));
            dateTimeColumn.setPrefWidth(200);

            // Reply Button
            TableColumn<String[], Void> replyColumn = new TableColumn<>("Reply");
            replyColumn.setCellFactory(param -> new TableCell<>() {
                private final Button replyButton = new Button("Reply");

                {
                    replyButton.setOnAction(event -> {
                        String[] feedback = getTableView().getItems().get(getIndex());
                        
                        // Extract ID from feedback array
                        int questionID = Integer.parseInt(feedback[1]);
                        String reviewer = feedback[5];
                        
                        // Now create a reply dialog
                            TextInputDialog replyDialog = new TextInputDialog();
                            replyDialog.setTitle("Reply to Feedback");
                        replyDialog.setHeaderText("Reply to " + reviewer);
                        replyDialog.setContentText("Your reply:");
                        
                        replyDialog.showAndWait().ifPresent(reply -> {
                            try {
                                // Send reply as a new feedback message
                                dbHelper3.insertFeedback(
                                    questionID,
                                    reviewer,  // Send to the original sender
                                    currentUser.getUserName(), // Current student is sending
                                    reply
                                );
                                
                                        showAlert("Reply sent successfully!", Alert.AlertType.INFORMATION);
                                        
                                // Refresh the inbox
                                List<String[]> updatedFeedback = dbHelper3.getFeedbackForUser(currentUser.getUserName());
                                inboxTable.setItems(FXCollections.observableArrayList(updatedFeedback));
                                
                                        } catch (SQLException ex) {
                                showAlert("Error sending reply: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        });
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(replyButton);
                    }
                }
            });

            inboxTable.getColumns().addAll(typeColumn, questionIDColumn, questionColumn, 
                                         feedbackColumn, fromColumn, dateTimeColumn, replyColumn);

            inboxTable.setItems(FXCollections.observableArrayList(feedbackMessages));
            
            feedbackLayout.getChildren().add(inboxTable);
            feedbackTab.setContent(feedbackLayout);
            tabPane.getTabs().add(feedbackTab);
        }
        
        // Chat Messages tab
        if (!generalChatMessages.isEmpty()) {
            Tab chatTab = new Tab("General Chat");
            VBox chatLayout = new VBox(10);
            chatLayout.setPadding(new Insets(10));
            
            // Group chats by sender
            Map<String, List<String[]>> chatBySender = new HashMap<>();
            
            for (String[] chat : generalChatMessages) {
                String sender = chat[1]; // Sender is at index 1
                if (!chatBySender.containsKey(sender)) {
                    chatBySender.put(sender, new ArrayList<>());
                }
                chatBySender.get(sender).add(chat);
            }
            
            // Create a ListView for chat contacts
            ListView<String> contactsList = new ListView<>();
            ObservableList<String> contacts = FXCollections.observableArrayList(chatBySender.keySet());
            contactsList.setItems(contacts);
            contactsList.setPrefWidth(200);
            
            // Chat display area
            VBox chatDisplayArea = new VBox(10);
            ScrollPane chatScrollPane = new ScrollPane(chatDisplayArea);
        chatScrollPane.setFitToWidth(true);
            chatScrollPane.setPrefHeight(300);
            
            // Chat input
            TextField chatInput = new TextField();
            chatInput.setPromptText("Type your message here...");
        Button sendButton = new Button("Send");
            HBox inputArea = new HBox(10, chatInput, sendButton);
            inputArea.setAlignment(Pos.CENTER);
            
            // Initially disable chat controls
            chatInput.setDisable(true);
            sendButton.setDisable(true);
            
            // Reference to current selected contact
            final String[] selectedContactRef = {null};
            
            // Handle contact selection
            contactsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedContactRef[0] = newVal;
                    chatDisplayArea.getChildren().clear();
                    
                    // Display messages with this contact
                    List<String[]> messages = chatBySender.get(newVal);
                    for (String[] message : messages) {
                        HBox messageBox = new HBox(10);
                        Label messageLabel = new Label(message[3] + "\n(" + message[4] + ")");
                        messageLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 5px; -fx-background-radius: 5px;");
                        messageLabel.setWrapText(true);
                        messageLabel.setMaxWidth(400);
                        
                        // Align based on sender (from me or to me)
                        boolean isFromMe = message[2].equals(currentUser.getUserName());
                        messageBox.setAlignment(isFromMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                        messageLabel.setStyle(messageLabel.getStyle() + 
                                         (isFromMe ? "-fx-background-color: #dcf8c6;" : "-fx-background-color: #f0f0f0;"));
                        
                        messageBox.getChildren().add(messageLabel);
                        chatDisplayArea.getChildren().add(messageBox);
                    }
                    
                    // Enable chat controls
                    chatInput.setDisable(false);
                    sendButton.setDisable(false);
                }
            });
            
            // Send button action
            sendButton.setOnAction(e -> {
                if (selectedContactRef[0] != null && !chatInput.getText().trim().isEmpty()) {
                    try {
                        String message = chatInput.getText().trim();
                        dbHelper3.insertGeneralChatMessage("Student", selectedContactRef[0], currentUser.getUserName(), message);
                        
                        // Add the message to the display
                        HBox messageBox = new HBox(10);
                        Label messageLabel = new Label(message + "\n(Now)");
                        messageLabel.setStyle("-fx-background-color: #dcf8c6; -fx-padding: 5px; -fx-background-radius: 5px;");
                        messageLabel.setWrapText(true);
                        messageLabel.setMaxWidth(400);
                        messageBox.setAlignment(Pos.CENTER_RIGHT);
                        messageBox.getChildren().add(messageLabel);
                        chatDisplayArea.getChildren().add(messageBox);
                        
                        // Clear input
                        chatInput.clear();
                        
                        // Also update the chat list
                        if (!chatBySender.containsKey(selectedContactRef[0])) {
                            chatBySender.put(selectedContactRef[0], new ArrayList<>());
                            contacts.add(selectedContactRef[0]);
                        }
                        
                        // Create a temporary message entry
                        String[] newMessage = new String[] {
                            "Chat", selectedContactRef[0], currentUser.getUserName(), message, 
                            "Just now", Integer.toString(new Date().hashCode())
                        };
                        chatBySender.get(selectedContactRef[0]).add(newMessage);
                        
                    } catch (SQLException ex) {
                        showAlert("Error sending message: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
            
            // Create split pane for contacts and chat
            SplitPane chatSplitPane = new SplitPane();
            chatSplitPane.getItems().addAll(contactsList, new VBox(10, chatScrollPane, inputArea));
            chatSplitPane.setDividerPositions(0.3);
            
            chatLayout.getChildren().add(chatSplitPane);
            chatTab.setContent(chatLayout);
            tabPane.getTabs().add(chatTab);
        }
        
        Scene inboxScene = new Scene(tabPane, 1000, 500);
        inboxStage.setScene(inboxScene);
        inboxStage.show();
    }

    /**
     * Applies the search filter to the question table.
     * 
     * @param keyword The search keyword
     * @param filterType The filter type (All, Answered, Unanswered, Reviewer)
     * @param filterValue The filter value (for Reviewer filter)
     * @param questionTable The table to update with filtered results
     */
    private void applySearchFilter(String keyword, String filterType, String filterValue, TableView<Question> questionTable) {
        try {
            List<Question> filteredQuestions = dbHelper3.searchQuestions(keyword, filterType, filterValue);
            questionTable.setItems(FXCollections.observableArrayList(filteredQuestions));
        } catch (SQLException ex) {
            showAlert("Error applying search filter: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Convert user-friendly filter to backend filter type
    private String mapFilterType(String userFriendlyFilter) {
        switch (userFriendlyFilter) {
            case "Questions with Answers":
                return "Answered";
            case "Questions without Answers":
                return "Unanswered";
            case "Questions by Reviewer":
                return "Reviewer";
            default:
                return "All";
        }
    }

    /**
     * Displays a reviewer's profile in a dialog window.
     * 
     * @param reviewerName The username of the reviewer
     */
    private void showReviewerProfile(String reviewerName) {
        try {
            // Get the reviewer's profile from the database
            ReviewerProfile profile = dbHelper3.getReviewerProfile(reviewerName);
            
            if (profile == null) {
                showAlert("Profile not found for " + reviewerName, Alert.AlertType.WARNING);
                return;
            }
            
            // Create a new stage for the profile
            Stage profileStage = new Stage();
            profileStage.setTitle("Reviewer Profile: " + reviewerName);
            
            VBox layout = new VBox(15);
            layout.setPadding(new Insets(20));
            layout.setAlignment(Pos.CENTER_LEFT);
            
            // Reviewer name
            Label nameLabel = new Label("Reviewer: " + reviewerName);
            nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            // Statistics
            Label statsLabel = new Label("Statistics");
            statsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
            
            Label reviewsLabel = new Label("Total Reviews: " + profile.getTotalReviews());
            Label ratingLabel = new Label(String.format("Average Rating: %.1f", profile.getAverageRating()));
            
            // Experience
            Label expTitleLabel = new Label("Experience");
            expTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
            
            TextArea expArea = new TextArea(profile.getExperience());
            expArea.setEditable(false);
            expArea.setWrapText(true);
            expArea.setPrefRowCount(4);
            expArea.setPrefWidth(400);
            
            // Background
            Label bgTitleLabel = new Label("Background");
            bgTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
            
            TextArea bgArea = new TextArea(profile.getBackground());
            bgArea.setEditable(false);
            bgArea.setWrapText(true);
            bgArea.setPrefRowCount(4);
            bgArea.setPrefWidth(400);
            
            // Close button
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> profileStage.close());
            
            // Add all components to the layout
            layout.getChildren().addAll(
                nameLabel,
                statsLabel, reviewsLabel, ratingLabel,
                expTitleLabel, expArea,
                bgTitleLabel, bgArea,
                closeButton
            );
            
            Scene scene = new Scene(layout, 450, 500);
            profileStage.setScene(scene);
            profileStage.show();
            
        } catch (SQLException ex) {
            showAlert("Error loading reviewer profile: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

}