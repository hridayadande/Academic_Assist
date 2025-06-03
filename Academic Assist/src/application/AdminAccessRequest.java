package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;

public class AdminAccessRequest {
    private DatabaseHelper dbHelper;
    private String username;

    public AdminAccessRequest(DatabaseHelper dbHelper, String username) {
        this.dbHelper = dbHelper;
        this.username = username;
    }

    public void showRequestDialog(Stage stage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Request Admin Access");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Please explain why you need admin access...");
        reasonArea.setPrefRowCount(5);

        Button submitButton = new Button("Submit Request");
        submitButton.setOnAction(e -> {
            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                showAlert("Please provide a reason for your request.", Alert.AlertType.WARNING);
                return;
            }

            try {
                dbHelper.submitAdminAccessRequest(username, reason);
                showAlert("Your request has been submitted successfully.", Alert.AlertType.INFORMATION);
                stage.close();
            } catch (Exception ex) {
                showAlert("Error submitting request: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        layout.getChildren().addAll(titleLabel, reasonArea, submitButton);
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Request Admin Access");
        stage.show();
    }

    public void showAdminRequestsView(Stage stage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Admin Access Requests");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<Request> requestTable = new TableView<>();
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Request, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));

        TableColumn<Request, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        reasonCol.setPrefWidth(250);

        TableColumn<Request, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate()));
        
        // Add a column to show if a request is reopened
        TableColumn<Request, Void> reopenedFromCol = new TableColumn<>("Reopened From");
        reopenedFromCol.setCellFactory(param -> new TableCell<>() {
            private final Hyperlink reopenedLink = new Hyperlink("View Original");
            
            {
                reopenedLink.setOnAction(event -> {
                    Request request = getTableView().getItems().get(getIndex());
                    showOriginalRequestDialog(request.getReopenedFromId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                Request request = getTableView().getItems().get(getIndex());
                if (request.isReopened()) {
                    setGraphic(reopenedLink);
                } else {
                    setGraphic(null);
                }
            }
        });
        reopenedFromCol.setPrefWidth(120);

        requestTable.getColumns().addAll(usernameCol, reasonCol, dateCol, reopenedFromCol);

        try {
            ObservableList<Request> requests = dbHelper.getAdminAccessRequests();
            requestTable.setItems(requests);
        } catch (Exception e) {
            showAlert("Error loading requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        HBox buttonBox = new HBox(10);
        Button approveButton = new Button("Approve");
        Button denyButton = new Button("Deny");
        Button editDescButton = new Button("Edit Reason");
        
        editDescButton.setOnAction(e -> {
            Request selected = requestTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditDescriptionDialog(selected, requestTable);
            } else {
                showAlert("Please select a request to edit.", Alert.AlertType.WARNING);
            }
        });

        approveButton.setOnAction(e -> {
            Request selected = requestTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    dbHelper.approveAdminAccessRequest(selected.getUsername());
                    showAlert("Request approved successfully.", Alert.AlertType.INFORMATION);
                    refreshTable(requestTable);
                } catch (Exception ex) {
                    showAlert("Error approving request: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Please select a request to approve.", Alert.AlertType.WARNING);
            }
        });

        denyButton.setOnAction(e -> {
            Request selected = requestTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    dbHelper.denyAdminAccessRequest(selected.getUsername());
                    showAlert("Request denied successfully.", Alert.AlertType.INFORMATION);
                    refreshTable(requestTable);
                } catch (Exception ex) {
                    showAlert("Error denying request: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Please select a request to deny.", Alert.AlertType.WARNING);
            }
        });

        buttonBox.getChildren().addAll(approveButton, denyButton, editDescButton);

        layout.getChildren().addAll(titleLabel, requestTable, buttonBox);
        Scene scene = new Scene(layout, 700, 400);
        stage.setScene(scene);
        stage.setTitle("Manage Admin Access Requests");
        stage.show();
    }

    private void refreshTable(TableView<Request> table) {
        try {
            ObservableList<Request> requests = dbHelper.getAdminAccessRequests();
            table.setItems(requests);
        } catch (Exception e) {
            showAlert("Error refreshing requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    /**
     * Shows a dialog to edit the description of a request
     */
    private void showEditDescriptionDialog(Request request, TableView<Request> tableView) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Request Reason");
        dialog.setHeaderText("Edit Reason for " + request.getUsername());
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        TextArea descArea = new TextArea(request.getDescription());
        descArea.setPrefWidth(400);
        descArea.setPrefHeight(200);
        descArea.setWrapText(true);
        
        dialog.getDialogPane().setContent(descArea);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return descArea.getText().trim();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newDesc -> {
            if (!newDesc.isEmpty()) {
                try {
                    dbHelper.updateAdminRequestDescription(request.getId(), newDesc);
                    request.setDescription(newDesc);
                    tableView.refresh();
                    showAlert("Reason updated successfully.", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error updating reason: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    /**
     * Shows a dialog with information about the original request that was reopened
     * @param originalRequestId The ID of the original request
     */
    private void showOriginalRequestDialog(int originalRequestId) {
        try {
            Request originalRequest = dbHelper.getAdminRequestById(originalRequestId);
            if (originalRequest == null) {
                showAlert("Original request not found.", Alert.AlertType.WARNING);
                return;
            }
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Original Request Details");
            
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));
            
            // Create info labels
            Label titleLabel = new Label("Original Request Information");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            
            Label usernameLabel = new Label("Username: " + originalRequest.getUsername());
            Label reasonLabel = new Label("Reason: " + originalRequest.getDescription());
            Label dateLabel = new Label("Date: " + originalRequest.getDate());
            Label statusLabel = new Label("Status: Closed");
            
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> dialogStage.close());
            
            layout.getChildren().addAll(
                titleLabel,
                usernameLabel,
                reasonLabel,
                dateLabel,
                statusLabel,
                closeButton
            );
            
            Scene scene = new Scene(layout, 400, 300);
            dialogStage.setScene(scene);
            dialogStage.show();
            
        } catch (SQLException e) {
            showAlert("Error loading original request: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
} 