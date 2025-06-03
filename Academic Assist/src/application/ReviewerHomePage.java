package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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

import java.util.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;

/**
 * This page displays the reviewer interface with ability to view questions, 
 * provide answers, and add reviews.
 */
public class ReviewerHomePage {
    private DatabaseHelper dbHelper;
    private DatabaseHelper2 dbHelper2;
    private DatabaseHelper3 dbHelper3;
    private Reviewer reviewer;

    /**
     * Initializes the ReviewerHomePage with database connections.
     */
    public ReviewerHomePage() {
        this.dbHelper = new DatabaseHelper();
        this.dbHelper2 = new DatabaseHelper2();
        this.dbHelper3 = new DatabaseHelper3();
        this.reviewer = new Reviewer();
        try {
            dbHelper.connectToDatabase();
            dbHelper2.connectToDatabase();
            dbHelper3.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the reviewer page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param user The user object containing user information and roles
     */
    public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/reviewer.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true) // Scale image to fit
        );

        // Apply background to VBox
        layout.setBackground(new Background(background));
        
        // Label to display Hello user with their name
        Label userLabel = new Label("Hello, " + user.getfirstName() + "! (Reviewer)");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Create TableView for questions
        TableView<Question> questionTable = new TableView<>();
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Define table columns - same as StudentHomePage
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

        // Initial load of questions
        refreshQuestionTable(questionTable);

        // Buttons for reviewer functionality - limited as per requirements
        Button viewAnswersButton = new Button("Answer");
        Button searchQuestionButton = new Button("Search Questions");
        Button addReviewButton = new Button("Add Review");
        Button viewReviewsButton = new Button("View Reviews");
        Button inboxButton = new Button("Inbox");
        Button myReviewsButton = new Button("My Reviews");
        Button profileButton = new Button("My Profile");
        Button backButton = new Button("Back");

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");
        buttonBox.getChildren().addAll(viewAnswersButton, searchQuestionButton, 
                addReviewButton, viewReviewsButton, myReviewsButton, inboxButton, 
                profileButton, backButton);

        // View Answers button action
        viewAnswersButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                showAnswersDialog(selectedQuestion, user);
            } else {
                showAlert("Please select a question first.", Alert.AlertType.WARNING);
            }
        });

        // Search question button action
        searchQuestionButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Search Questions");
            dialog.setHeaderText("Enter search keyword:");
            dialog.setContentText("Keyword:");

            dialog.showAndWait().ifPresent(keyword -> {
                try {
                    List<Question> allQuestions = dbHelper2.getAllQuestions();
                    List<Question> filteredQuestions = allQuestions.stream()
                        .filter(q -> q.getBodyText().toLowerCase().contains(keyword.toLowerCase()))
                        .toList();
                    questionTable.setItems(FXCollections.observableArrayList(filteredQuestions));
                } catch (SQLException ex) {
                    showAlert("Error searching questions: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            });
        });

        // Add Review button action
        addReviewButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                reviewer.addReviewToQuestion(selectedQuestion.getQuestionID(), user);
            } else {
                showAlert("Please select a question to review.", Alert.AlertType.WARNING);
            }
        });

        // View Reviews button action
        viewReviewsButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                reviewer.viewReviewsForQuestion(selectedQuestion.getQuestionID(), user);
            } else {
                showAlert("Please select a question to view reviews.", Alert.AlertType.WARNING);
            }
        });
        myReviewsButton.setOnAction(e -> showMyReviews(user));
        
        inboxButton.setOnAction(e -> showInbox(user));
        profileButton.setOnAction(e -> showProfileDialog(user));

        // Back button action
        backButton.setOnAction(e -> new SelectRole().show(primaryStage, user, user.getRole()));

        // Add all components to layout
        layout.getChildren().addAll(userLabel, questionTable, buttonBox);

        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("Reviewer Page");
    }

    /**
     * Displays a dialog showing answers for a specific question.
     * 
     * @param question The question whose answers are being displayed.
     * @param user The current user viewing the answers.
     */
    private void showAnswersDialog(Question question, User user) {
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

        // Button container for reviewer functionality in answer dialog - limited as per requirements
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        Button searchAnswerButton = new Button("Search Answer");
        Button addReviewButton = new Button("Add Review");
        Button viewReviewsButton = new Button("View Reviews");

        buttonBox.getChildren().addAll(searchAnswerButton, addReviewButton, viewReviewsButton);

        // Search Answer button action
        searchAnswerButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Search Answer");
            dialog.setHeaderText("Enter search keyword:");
            dialog.setContentText("Keyword:");

            dialog.showAndWait().ifPresent(keyword -> {
                try {
                    List<Answer> allAnswers = dbHelper2.getAnswersForQuestion(question.getQuestionID());
                    List<Answer> filteredAnswers = allAnswers.stream()
                        .filter(answer -> answer.getBodyText().toLowerCase().contains(keyword.toLowerCase()))
                        .toList();
                    answerTable.setItems(FXCollections.observableArrayList(filteredAnswers));
                } catch (SQLException ex) {
                    showAlert("Error searching answers: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            });
        });

        // Add Review to Answer button action
        addReviewButton.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                reviewer.addReviewToAnswer(selectedAnswer, user);
            } else {
                showAlert("Please select an answer to review.", Alert.AlertType.WARNING);
            }
        });

        // View Reviews button action
        viewReviewsButton.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                reviewer.viewReviewsForAnswer(selectedAnswer, user);
            } else {
                showAlert("Please select an answer to view reviews.", Alert.AlertType.WARNING);
            }
        });

        dialogLayout.getChildren().addAll(statusBox, answerTable, buttonBox);
        dialogStage.setScene(new Scene(dialogLayout, 600, 400));
        dialogStage.setTitle("Answers for Question: " + question.getBodyText());
        dialogStage.show();
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
    
    private void showInbox(User user) {
    	
        try {
            // Get feedback messages
            List<String[]> feedbackList = dbHelper3.getReviewFeedbackForReviewer(user.getUserName());
            
            // Get chat messages
            List<String[]> chatMessages = dbHelper3.getChatMessagesForReviewer(user.getUserName());
            
            // Combine both lists
            List<String[]> allMessages = new ArrayList<>();
            allMessages.addAll(feedbackList);
            allMessages.addAll(chatMessages);
            
            if (allMessages.isEmpty()) {
                showAlert("Your inbox is empty.", Alert.AlertType.INFORMATION);
                return;
            }

            Stage inboxStage = new Stage();
            inboxStage.setTitle("Reviewer Inbox");

            TableView<String[]> inboxTable = new TableView<>();

            TableColumn<String[], String> typeColumn = new TableColumn<>("Type");
            typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));

            TableColumn<String[], String> contentColumn = new TableColumn<>("Question/Answer/Chat");
            contentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
            contentColumn.setPrefWidth(300);

            TableColumn<String[], String> messageColumn = new TableColumn<>("Message");
            messageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
            messageColumn.setPrefWidth(300);

            TableColumn<String[], String> fromColumn = new TableColumn<>("From");
            fromColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));

            TableColumn<String[], String> dateColumn = new TableColumn<>("Date-Time");
            dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[5]));

            TableColumn<String[], Void> replyColumn = new TableColumn<>("Reply");
            replyColumn.setCellFactory(param -> new TableCell<>() {
                private final Button replyBtn = new Button("Reply");

                {
                    replyBtn.setOnAction(e -> {
                        String[] row = getTableView().getItems().get(getIndex());
                        String type = row[0];
                        String messageId = row[2];
                        String sentTo = row[4];
                        
                        TextInputDialog replyDialog = new TextInputDialog();
                        replyDialog.setTitle("Reply");
                        replyDialog.setHeaderText("Replying to: " + sentTo);
                        replyDialog.setContentText("Enter your reply:");

                        replyDialog.showAndWait().ifPresent(replyText -> {
                            if (!replyText.trim().isEmpty()) {
                                try {
                                    if (type.equals("Feedback")) {
                                        // Handle feedback reply
                                        int parentID = Integer.parseInt(messageId);
                                        int reviewID = Integer.parseInt(row[6]);
                                        int targetID = Integer.parseInt(row[7]);
                                        boolean isAnswer = Boolean.parseBoolean(row[8]);
                                        
                                        dbHelper3.insertReviewReply(parentID, reviewID, targetID, isAnswer,
                                                user.getUserName(), sentTo, replyText);
                                    } else if (type.equals("Chat")) {
                                        // Handle chat reply
                                        dbHelper3.insertGeneralChatMessage("Reviewer", sentTo, user.getUserName(), replyText);
                                    }
                                    
                                    showAlert("Reply sent successfully!", Alert.AlertType.INFORMATION);
                                    
                                    // Refresh the inbox
                                    List<String[]> updatedFeedbackList = dbHelper3.getReviewFeedbackForReviewer(user.getUserName());
                                    List<String[]> updatedChatMessages = dbHelper3.getChatMessagesForReviewer(user.getUserName());
                                    List<String[]> updatedAllMessages = new ArrayList<>();
                                    updatedAllMessages.addAll(updatedFeedbackList);
                                    updatedAllMessages.addAll(updatedChatMessages);
                                    inboxTable.setItems(FXCollections.observableArrayList(updatedAllMessages));
                                } catch (Exception ex) {
                                    showAlert("Error sending reply: " + ex.getMessage(), Alert.AlertType.ERROR);
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
                        String[] row = getTableView().getItems().get(getIndex());
                        String type = row[0];
                        setGraphic(type.equals("Feedback") || type.equals("Chat") ? replyBtn : null);
                    }
                }
            });

            inboxTable.getColumns().setAll(typeColumn, contentColumn, messageColumn, fromColumn, dateColumn, replyColumn);

            inboxTable.setItems(FXCollections.observableArrayList(allMessages));

            VBox inboxLayout = new VBox(10, inboxTable);
            inboxLayout.setStyle("-fx-padding: 20;");
            inboxStage.setScene(new Scene(inboxLayout, 1000, 500));
            inboxStage.show();

        } catch (SQLException ex) {
            showAlert("Error fetching inbox: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void showMyReviews(User user) {
        try {
            List<String[]> reviewList = dbHelper3.getReviewsByReviewer(user.getUserName());

            Stage reviewStage = new Stage();
            reviewStage.setTitle("My Reviews");

            TableView<String[]> reviewTable = new TableView<>();

            TableColumn<String[], String> typeColumn = new TableColumn<>("Type");
            typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));

            TableColumn<String[], String> idColumn = new TableColumn<>("Target ID");
            idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

            TableColumn<String[], String> contentColumn = new TableColumn<>("Content");
            contentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
            contentColumn.setPrefWidth(300);

            TableColumn<String[], String> reviewColumn = new TableColumn<>("Review");
            reviewColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
            reviewColumn.setPrefWidth(300);

            TableColumn<String[], String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));

            reviewTable.getColumns().setAll(typeColumn, idColumn, contentColumn, reviewColumn, dateColumn);
            reviewTable.setItems(FXCollections.observableArrayList(reviewList));

            VBox reviewLayout = new VBox(10, reviewTable);
            reviewLayout.setStyle("-fx-padding: 20;");
            reviewStage.setScene(new Scene(reviewLayout, 1000, 500));
            reviewStage.show();

        } catch (SQLException ex) {
            showAlert("Error fetching reviews: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    

    private void showProfileDialog(User user) {
        Stage profileStage = new Stage();
        profileStage.setTitle("Reviewer Profile");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");

        // Experience section
        Label expLabel = new Label("Your Experience:");
        TextArea expArea = new TextArea();
        expArea.setWrapText(true);
        expArea.setPrefRowCount(3);

        // Background section
        Label bgLabel = new Label("Your Background:");
        TextArea bgArea = new TextArea();
        bgArea.setWrapText(true);
        bgArea.setPrefRowCount(3);

        // Stats section
        Label statsLabel = new Label("Your Statistics");
        statsLabel.setStyle("-fx-font-weight: bold;");
        Label reviewsLabel = new Label("Total Reviews: 0");
        Label ratingLabel = new Label("Average Rating: 0.0");

        // Load existing profile if it exists
        try {
            ReviewerProfile profile = dbHelper3.getReviewerProfile(user.getUserName());
            if (profile != null) {
                expArea.setText(profile.getExperience());
                bgArea.setText(profile.getBackground());
                reviewsLabel.setText("Total Reviews: " + profile.getTotalReviews());
                ratingLabel.setText(String.format("Average Rating: %.1f", profile.getAverageRating()));
            }
        } catch (SQLException e) {
            showAlert("Error loading profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Save button
        Button saveButton = new Button("Save Profile");
        saveButton.setOnAction(e -> {
            try {
                dbHelper3.updateReviewerProfile(
                    user.getUserName(),
                    expArea.getText(),
                    bgArea.getText()
                );
                showAlert("Profile updated successfully!", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                showAlert("Error saving profile: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        // Feedback section
        Label feedbackLabel = new Label("Feedback Received");
        feedbackLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");
        
        TableView<String[]> feedbackTable = new TableView<>();
        
        TableColumn<String[], String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        
        TableColumn<String[], String> feedbackCol = new TableColumn<>("Feedback");
        feedbackCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        feedbackCol.setPrefWidth(200);
        
        TableColumn<String[], String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        
        TableColumn<String[], String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        
        feedbackTable.getColumns().addAll(typeCol, feedbackCol, fromCol, dateCol);
        
        // Load feedback
        try {
            List<String[]> feedback = dbHelper3.getReviewerFeedback(user.getUserName());
            feedbackTable.setItems(FXCollections.observableArrayList(feedback));
        } catch (SQLException e) {
            showAlert("Error loading feedback: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        layout.getChildren().addAll(
            expLabel, expArea,
            bgLabel, bgArea,
            statsLabel, reviewsLabel, ratingLabel,
            saveButton,
            feedbackLabel, feedbackTable
        );

        Scene scene = new Scene(layout, 600, 800);
        profileStage.setScene(scene);
        profileStage.show();
    }
}