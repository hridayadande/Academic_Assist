package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import javafx.util.Pair;
import java.util.stream.Collectors;
import application.Request;

public class InstructorHomePage {
    private DatabaseHelper dbHelper;
    private DatabaseHelper2 dbHelper2;
    private DatabaseHelper3 dbHelper3;
    private User currentUser;

    public InstructorHomePage() {
        this.dbHelper  = new DatabaseHelper();
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

    public void show(Stage primaryStage, User user) {
        this.currentUser = user;
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Image backgroundImage = new Image(getClass().getResource("/instructor.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true)
        );
        layout.setBackground(new Background(background));

        Label userLabel = new Label("Hello, " + user.getfirstName() + "! (Instructor)");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<ReviewerRequest> requestTable = new TableView<>();
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReviewerRequest,String> studentColumn = new TableColumn<>("Student");
        studentColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getStudentName()));

        TableColumn<ReviewerRequest,String> messageColumn = new TableColumn<>("Request Message");
        messageColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getRequestMessage()));
        messageColumn.setPrefWidth(300);

        TableColumn<ReviewerRequest,String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getRequestDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        TableColumn<ReviewerRequest,String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty("Pending"));

        requestTable.getColumns().addAll(studentColumn,messageColumn,dateColumn,statusColumn);

        try {
            ObservableList<ReviewerRequest> requests =
                    FXCollections.observableArrayList(
                            dbHelper.getReviewerRequestsForInstructor(user.getUserName())
                    );
            requestTable.setItems(requests);
        } catch (SQLException e) {
            showAlert("Error loading reviewer requests: "+e.getMessage(),Alert.AlertType.ERROR);
        }

        Button viewStudentActivityButton = new Button("View Student Activity");
        viewStudentActivityButton.setOnAction(e -> {
            ReviewerRequest selected = requestTable.getSelectionModel().getSelectedItem();
            if (selected!=null) {
                showStudentActivityDialog(selected.getStudentName());
            } else showAlert("Please select a request to view student activity.",Alert.AlertType.WARNING);
        });

        Button acceptButton = new Button("Accept Request");
        acceptButton.setOnAction(e -> {
            ReviewerRequest selected = requestTable.getSelectionModel().getSelectedItem();
            if (selected!=null) {
                try {
                    dbHelper.updateReviewerRequestStatus(selected.getRequestID(),"ACCEPTED");
                    String newRole = dbHelper.getUserRole(selected.getStudentName())+",Reviewer";
                    dbHelper.updateUserRole(selected.getStudentName(),newRole);
                    showAlert("Request accepted! Student "+selected.getStudentName()+" is now a reviewer.",Alert.AlertType.INFORMATION);
                    requestTable.setItems(FXCollections.observableArrayList(
                            dbHelper.getReviewerRequestsForInstructor(user.getUserName())
                    ));
                } catch (SQLException ex) {
                    showAlert("Error accepting request: "+ex.getMessage(),Alert.AlertType.ERROR);
                }
            } else showAlert("Please select a request to accept.",Alert.AlertType.WARNING);
        });

        Button inboxButton = new Button("Inbox");
        inboxButton.setOnAction(e -> showInboxDialog());

        Button restrictedStudentsButton = new Button("Restricted Students");
        restrictedStudentsButton.setOnAction(e -> showRestrictedStudentsDialog());

        Button flaggedActivityButton = new Button("Flagged Activity");
        flaggedActivityButton.setOnAction(e -> showFlaggedActivity());

        Button manageScoresButton = new Button("Manage Reviewer Scores");
        manageScoresButton.setOnAction(e -> showManageReviewerScoresDialog());

        Button viewAdminActionsButton = new Button("View Admin Actions");
        viewAdminActionsButton.setOnAction(e -> new AdminActionsDialog(user, dbHelper).show());
        
        Button viewClosedRequestsButton = new Button("View Closed Requests");
        viewClosedRequestsButton.setOnAction(e -> showClosedRequestsDialog());

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new SelectRole().show(primaryStage,user,user.getRole()));

        HBox buttonBox = new HBox(10,viewStudentActivityButton,acceptButton,inboxButton,
                restrictedStudentsButton,flaggedActivityButton,manageScoresButton,viewAdminActionsButton,viewClosedRequestsButton,backButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(userLabel,requestTable,buttonBox);
        layout.setPadding(new Insets(20));

        primaryStage.setScene(new Scene(layout,800,600));
        primaryStage.setTitle("Instructor Page - Reviewer Requests");
    }

    /**
     * Displays a dialog showing the student's questions and answers.
     * @param studentUsername The username of the student
     */
    private void showStudentActivityDialog(String studentUsername) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Student Activity: " + studentUsername);
        
        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(20));
        
        // Create tabs for questions and answers
        TabPane tabPane = new TabPane();
        
        // Questions tab
        Tab questionsTab = new Tab("Questions");
        VBox questionsLayout = new VBox(10);
        
        TableView<Question> questionsTable = new TableView<>();
        questionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Question, String> questionIdColumn = new TableColumn<>("ID");
        questionIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getQuestionID())));
        
        TableColumn<Question, String> questionTextColumn = new TableColumn<>("Question");
        questionTextColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBodyText()));
        questionTextColumn.setPrefWidth(300);
        
        TableColumn<Question, String> questionDateColumn = new TableColumn<>("Date");
        questionDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateCreated().toString()
            ));
        
        TableColumn<Question, String> questionStatusColumn = new TableColumn<>("Status");
        questionStatusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isResolved() ? "Resolved" : "Unresolved"
            ));
        
        questionsTable.getColumns().addAll(questionIdColumn, questionTextColumn, questionDateColumn, questionStatusColumn);
        
        try {
            List<Question> studentQuestions = dbHelper2.getQuestionsByStudent(studentUsername);
            questionsTable.setItems(FXCollections.observableArrayList(studentQuestions));
        } catch (SQLException e) {
            showAlert("Error loading student questions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        questionsLayout.getChildren().add(questionsTable);
        questionsTab.setContent(questionsLayout);
        
        // Answers tab
        Tab answersTab = new Tab("Answers");
        VBox answersLayout = new VBox(10);
        
        TableView<Answer> answersTable = new TableView<>();
        answersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Answer, String> answerIdColumn = new TableColumn<>("ID");
        answerIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getAnsID())));
        
        TableColumn<Answer, String> questionIdForAnswerColumn = new TableColumn<>("Question ID");
        questionIdForAnswerColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getQuestionID())));
        
        TableColumn<Answer, String> answerTextColumn = new TableColumn<>("Answer");
        answerTextColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBodyText()));
        answerTextColumn.setPrefWidth(300);
        
        TableColumn<Answer, String> answerDateColumn = new TableColumn<>("Date");
        answerDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateCreated().toString()
            ));
        
        answersTable.getColumns().addAll(answerIdColumn, questionIdForAnswerColumn, answerTextColumn, answerDateColumn);
        
        try {
            List<Answer> studentAnswers = dbHelper2.getAnswersByStudent(studentUsername);
            answersTable.setItems(FXCollections.observableArrayList(studentAnswers));
        } catch (SQLException e) {
            showAlert("Error loading student answers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        answersLayout.getChildren().add(answersTable);
        answersTab.setContent(answersLayout);
        
        // Add tabs to tab pane
        tabPane.getTabs().addAll(questionsTab, answersTab);
        
        // Close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialogStage.close());
        
        dialogLayout.getChildren().addAll(tabPane, closeButton);
        
        Scene dialogScene = new Scene(dialogLayout, 800, 500);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    /**
     * Displays a dialog showing the instructor's inbox with restriction requests.
     */
    private void showInboxDialog() {
        try {
            // Get feedback for the current instructor that contains restriction requests
            List<String[]> restrictionRequests = new ArrayList<>();
            List<String[]> allFeedback = dbHelper3.getFeedbackForUser(currentUser.getUserName());
            
            // Filter for restriction requests
            for (String[] feedback : allFeedback) {
                if (feedback[4].startsWith("[RESTRICT REQUEST]")) {
                    restrictionRequests.add(feedback);
                }
            }
            
            if (restrictionRequests.isEmpty()) {
                showAlert("No restriction requests in your inbox.", Alert.AlertType.INFORMATION);
                return;
            }
            
            // Create dialog
            Stage inboxStage = new Stage();
            inboxStage.setTitle("Inbox - Restriction Requests");
            
            VBox inboxLayout = new VBox(10);
            inboxLayout.setPadding(new Insets(20));
            
            // Create table
            TableView<String[]> inboxTable = new TableView<>();
            inboxTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            // Staff column
            TableColumn<String[], String> staffColumn = new TableColumn<>("Requested By");
            staffColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[5]));
            staffColumn.setPrefWidth(100);
            
            // Student column (extract from the message)
            TableColumn<String[], String> studentColumn = new TableColumn<>("Student");
            studentColumn.setCellValueFactory(cellData -> {
                String message = cellData.getValue()[4];
                String student = message.substring(message.indexOf("Student: ") + 9, message.indexOf(" - "));
                return new javafx.beans.property.SimpleStringProperty(student);
            });
            studentColumn.setPrefWidth(100);
            
            // Reason column
            TableColumn<String[], String> reasonColumn = new TableColumn<>("Reason");
            reasonColumn.setCellValueFactory(cellData -> {
                String message = cellData.getValue()[4];
                String reason = message.substring(message.indexOf(" - ") + 3);
                return new javafx.beans.property.SimpleStringProperty(reason);
            });
            reasonColumn.setPrefWidth(300);
            
            // Date column
            TableColumn<String[], String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[6]));
            dateColumn.setPrefWidth(150);
            
            // Question ID column
            TableColumn<String[], String> questionColumn = new TableColumn<>("Question ID");
            questionColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[1]));
            questionColumn.setPrefWidth(80);
            
            // Action column
            TableColumn<String[], Void> actionColumn = new TableColumn<>("Action");
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button restrictButton = new Button("Restrict User");
                
                {
                    restrictButton.setOnAction(event -> {
                        String[] request = getTableView().getItems().get(getIndex());
                        String message = request[4];
                        String student = message.substring(message.indexOf("Student: ") + 9, message.indexOf(" - "));
                        
                        // Show confirmation dialog
                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Confirm Restriction");
                        confirmation.setHeaderText("Restrict " + student + " from posting?");
                        confirmation.setContentText("This will prevent " + student + " from posting new questions or answers.");
                        
                        Optional<ButtonType> result = confirmation.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            try {
                                // Update user role to add "Restricted" flag
                                String currentRole = dbHelper.getUserRole(student);
                                System.out.println("Restricting user: " + student);
                                System.out.println("Current role: " + currentRole);
                                
                                if (!currentRole.contains("Restricted")) {
                                    String newRole = currentRole + ",Restricted";
                                    System.out.println("Setting new role: " + newRole);
                                    dbHelper.updateUserRole(student, newRole);
                                    showAlert("User " + student + " has been restricted from posting.", 
                                             Alert.AlertType.INFORMATION);
                                    
                                    // Remove this request from the table
                                    getTableView().getItems().remove(getIndex());
                                } else {
                                    showAlert("User " + student + " is already restricted.", 
                                             Alert.AlertType.INFORMATION);
                                }
                            } catch (SQLException ex) {
                                showAlert("Error restricting user: " + ex.getMessage(), 
                                         Alert.AlertType.ERROR);
                            }
                        }
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : restrictButton);
                }
            });
            actionColumn.setPrefWidth(120);
            
            inboxTable.getColumns().addAll(staffColumn, studentColumn, reasonColumn, dateColumn, 
                                         questionColumn, actionColumn);
            inboxTable.setItems(FXCollections.observableArrayList(restrictionRequests));
            
            // Close button
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> inboxStage.close());
            
            inboxLayout.getChildren().addAll(inboxTable, closeButton);
            Scene inboxScene = new Scene(inboxLayout, 900, 500);
            inboxStage.setScene(inboxScene);
            inboxStage.show();
            
        } catch (SQLException ex) {
            showAlert("Error loading inbox: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Displays a dialog showing all restricted students with option to unrestrict them.
     */
    private void showRestrictedStudentsDialog() {
        try {
            // Get all restricted students from the database
            List<String[]> restrictedStudents = dbHelper.getRestrictedUsers();
            
            if (restrictedStudents.isEmpty()) {
                showAlert("No restricted students found.", Alert.AlertType.INFORMATION);
                return;
            }
            
            // Create dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Restricted Students");
            
            VBox dialogLayout = new VBox(10);
            dialogLayout.setPadding(new Insets(20));
            
            Label titleLabel = new Label("Students with Restricted Status");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            
            // Create table for restricted students
            TableView<String[]> studentsTable = new TableView<>();
            studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            // Username column
            TableColumn<String[], String> usernameColumn = new TableColumn<>("Username");
            usernameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[0]));
            usernameColumn.setPrefWidth(150);
            
            // Name column
            TableColumn<String[], String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[1] + " " + cellData.getValue()[2]));
            nameColumn.setPrefWidth(200);
            
            // Roles column
            TableColumn<String[], String> rolesColumn = new TableColumn<>("Roles");
            rolesColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[3]));
            rolesColumn.setPrefWidth(200);
            
            // Action column for unrestricting
            TableColumn<String[], Void> actionColumn = new TableColumn<>("Action");
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button unrestrictButton = new Button("Unrestrict");
                
                {
                    unrestrictButton.setOnAction(event -> {
                        String[] student = getTableView().getItems().get(getIndex());
                        String username = student[0];
                        String currentRoles = student[3];
                        
                        // Show confirmation dialog
                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Confirm Unrestriction");
                        confirmation.setHeaderText("Unrestrict " + username + "?");
                        confirmation.setContentText("This will allow the student to post questions and answers again.");
                        
                        Optional<ButtonType> result = confirmation.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            try {
                                // Remove the Restricted flag from the roles
                                String newRoles = currentRoles.replace(",Restricted", "").replace("Restricted,", "").replace("Restricted", "");
                                if (newRoles.isEmpty()) {
                                    newRoles = "Student"; // Default to Student if all roles were removed
                                }
                                
                                System.out.println("Unrestricting user: " + username);
                                System.out.println("Current roles: " + currentRoles);
                                System.out.println("New roles: " + newRoles);
                                
                                // Update the user's role in the database
                                dbHelper.updateUserRole(username, newRoles);
                                
                                showAlert("User " + username + " has been unrestricted.", Alert.AlertType.INFORMATION);
                                
                                // Remove from the table
                                getTableView().getItems().remove(getIndex());
                                
                                // If the table is now empty, close the dialog
                                if (getTableView().getItems().isEmpty()) {
                                    dialogStage.close();
                                    showAlert("No more restricted students.", Alert.AlertType.INFORMATION);
                                }
                            } catch (SQLException ex) {
                                showAlert("Error unrestricting user: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : unrestrictButton);
                }
            });
            actionColumn.setPrefWidth(100);
            
            studentsTable.getColumns().addAll(usernameColumn, nameColumn, rolesColumn, actionColumn);
            studentsTable.setItems(FXCollections.observableArrayList(restrictedStudents));
            
            // Close button
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> dialogStage.close());
            
            dialogLayout.getChildren().addAll(titleLabel, studentsTable, closeButton);
            Scene dialogScene = new Scene(dialogLayout, 700, 500);
            dialogStage.setScene(dialogScene);
            dialogStage.show();
            
        } catch (SQLException ex) {
            showAlert("Error loading restricted students: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
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
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[1]));
            
            TableColumn<String[], String> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[2]));
            
            TableColumn<String[], String> contentColumn = new TableColumn<>("Content");
            contentColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[6]));
            contentColumn.setPrefWidth(300);
            
            TableColumn<String[], String> flaggedByColumn = new TableColumn<>("Flagged By");
            flaggedByColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[3]));
            
            TableColumn<String[], String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[4]));
            
            TableColumn<String[], String> reasonColumn = new TableColumn<>("Reason");
            reasonColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[5]));
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
     * Shows a dialog for managing reviewer scores.
     */
    private void showManageReviewerScoresDialog() {
        Stage scoresStage = new Stage();
        scoresStage.setTitle("Manage Reviewer Scores");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        // Create table for reviewers and their scores
        TableView<String[]> scoresTable = new TableView<>();
        
        // Reviewer column
        TableColumn<String[], String> reviewerCol = new TableColumn<>("Reviewer");
        reviewerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        
        // Score column
        TableColumn<String[], String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        
        // Set Score column
        TableColumn<String[], Void> setScoreCol = new TableColumn<>("Set Score");
        setScoreCol.setCellFactory(param -> new TableCell<>() {
            private final Button setScoreButton = new Button("Set Score");
            
            {
                setScoreButton.setOnAction(event -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    String reviewerName = row[0];
                    showSetScoreDialog(reviewerName);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(setScoreButton);
                }
            }
        });
        
        scoresTable.getColumns().addAll(reviewerCol, scoreCol, setScoreCol);
        
        // Load reviewers and their scores
        try {
            List<String[]> reviewers = dbHelper3.getAllReviewers().stream()
                .map(name -> {
					try {
						return new String[]{name, String.valueOf(dbHelper3.getReviewerWeight(currentUser.getUserName(), name))};
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				})
                .collect(Collectors.toList());
            scoresTable.setItems(FXCollections.observableArrayList(reviewers));
        } catch (SQLException e) {
            showAlert("Error loading reviewer scores: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        layout.getChildren().add(scoresTable);
        
        Scene scene = new Scene(layout, 500, 400);
        scoresStage.setScene(scene);
        scoresStage.show();
    }
    
    /**
     * Shows a dialog to set a score for a specific reviewer.
     * 
     * @param reviewerName The name of the reviewer to score
     */
    private void showSetScoreDialog(String reviewerName) {
        Dialog<Pair<Pair<Integer, Integer>, String>> dialog = new Dialog<>();
        dialog.setTitle("Set Reviewer Score");
        dialog.setHeaderText("Set ratings for " + reviewerName + "\nEach criteria is 1-5 (5 = best)");

        ComboBox<Integer> professionalismBox = new ComboBox<>();
        professionalismBox.getItems().addAll(1, 2, 3, 4, 5);
        professionalismBox.setPromptText("Professionalism");

        ComboBox<Integer> responsivenessBox = new ComboBox<>();
        responsivenessBox.getItems().addAll(1, 2, 3, 4, 5);
        responsivenessBox.setPromptText("Responsiveness");

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Provide feedback for the reviewer");
        feedbackArea.setPrefRowCount(3);

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Professionalism:"), professionalismBox,
            new Label("Responsiveness:"), responsivenessBox,
            new Label("Feedback:"), feedbackArea
        );
        dialog.getDialogPane().setContent(content);

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return new Pair<>(
                    new Pair<>(professionalismBox.getValue(), responsivenessBox.getValue()),
                    feedbackArea.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                int professionalism = result.getKey().getKey();
                int responsiveness = result.getKey().getValue();
                int averageScore = (professionalism + responsiveness) / 2;

                dbHelper3.setReviewerWeight(currentUser.getUserName(), reviewerName, averageScore);

                if (!result.getValue().trim().isEmpty()) {
                    dbHelper3.insertFeedbackForReviewer(
                        reviewerName,
                        currentUser.getUserName(),
                        result.getValue()
                    );
                }

                showAlert("Score and feedback set successfully!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Error setting score and feedback: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
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
            
            // Create a label for instructions
            Label instructionsLabel = new Label("Select a request to view its details. You can reopen or update the reason for a request.");
            instructionsLabel.setStyle("-fx-font-weight: bold;");
            
            TableView<Request> requestsTable = new TableView<>();
            
            // Show description in the Reason column to match View Admin Requests
            TableColumn<Request, String> reasonColumn = new TableColumn<>("Reason");
            reasonColumn.setCellValueFactory(cell -> 
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getDescription()));
            reasonColumn.setPrefWidth(300);
            
            TableColumn<Request, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(cell -> 
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getDate()));
            dateColumn.setPrefWidth(150);
            
            requestsTable.getColumns().addAll(reasonColumn, dateColumn);
            requestsTable.setItems(userRequests);
            
            // Reason editing area
            Label reasonEditLabel = new Label("Edit Reason:");
            reasonEditLabel.setStyle("-fx-font-weight: bold;");
            
            TextArea reasonEditArea = new TextArea();
            reasonEditArea.setWrapText(true);
            reasonEditArea.setPrefRowCount(5);
            reasonEditArea.setEditable(true);
            
            // Button to update reason
            Button updateReasonButton = new Button("Update Reason");
            updateReasonButton.setDisable(true);
            
            // Button to reopen request
            Button reopenButton = new Button("Reopen Request");
            reopenButton.setDisable(true);
            
            // Close button
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> dialogStage.close());
            
            // HBox for buttons
            HBox buttonBox = new HBox(10, updateReasonButton, reopenButton, closeButton);
            buttonBox.setAlignment(Pos.CENTER);
            
            // When a request is selected, show its description
            requestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    reasonEditArea.setText(newVal.getDescription());
                    updateReasonButton.setDisable(false);
                    reopenButton.setDisable(false);
                } else {
                    reasonEditArea.clear();
                    updateReasonButton.setDisable(true);
                    reopenButton.setDisable(true);
                }
            });
            
            // Update reason button action
            updateReasonButton.setOnAction(e -> {
                Request selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
                if (selectedRequest != null) {
                    String newReason = reasonEditArea.getText().trim();
                    if (newReason.isEmpty()) {
                        showAlert("Reason cannot be empty.", Alert.AlertType.WARNING);
                        return;
                    }
                    
                    try {
                        dbHelper.updateAdminRequestDescription(selectedRequest.getId(), newReason);
                        
                        // Update the description in the local object
                        selectedRequest.setDescription(newReason);
                        
                        // Refresh table to show updated reason
                        requestsTable.refresh();
                        
                        showAlert("Reason updated successfully.", Alert.AlertType.INFORMATION);
                    } catch (SQLException ex) {
                        showAlert("Error updating reason: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
            
            // Reopen button action
            reopenButton.setOnAction(e -> {
                Request selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
                if (selectedRequest != null) {
                    try {
                        dbHelper.reopenAdminRequest(selectedRequest.getId());
                        
                        showAlert("Request reopened successfully. It will now appear in the admin requests list.", 
                                 Alert.AlertType.INFORMATION);
                        
                        // Remove from the current table
                        requestsTable.getItems().remove(selectedRequest);
                        
                        if (requestsTable.getItems().isEmpty()) {
                            dialogStage.close();
                        }
                    } catch (SQLException ex) {
                        showAlert("Error reopening request: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
            
            layout.getChildren().addAll(
                instructionsLabel, 
                requestsTable, 
                reasonEditLabel, 
                reasonEditArea, 
                buttonBox
            );
            
            Scene scene = new Scene(layout, 600, 500);
            dialogStage.setScene(scene);
            dialogStage.show();
            
        } catch (SQLException e) {
            showAlert("Error loading closed requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message);
        alert.showAndWait();
    }
}