package databasePart1;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import application.Question;
import application.Answer;
import application.ReviewerProfile;

/**
 * The DatabaseHelper3 class is responsible for managing review, feedback, and chat operations
 * in the database. This class handles all review and communication related database interactions.
 */
public class DatabaseHelper3 {

    // JDBC driver name and database URL 
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

    // Database credentials 
    static final String USER = "sa"; 
    static final String PASS = ""; 

    private Connection connection = null;
    private Statement statement = null; 
 
    /**
     * Connects to the database.
     */
    public void connectToDatabase() throws SQLException {
        try {
            // Load the JDBC driver
            Class.forName(JDBC_DRIVER); 
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 
            
            // Create ReviewerProfiles table if it doesn't exist
            statement.execute("CREATE TABLE IF NOT EXISTS ReviewerProfiles (" +
                "userName VARCHAR(50) PRIMARY KEY," +
                "experience TEXT," +
                "background TEXT," +
                "lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * This method ensures that the database connection is open.
     * If the connection is null or closed, it will attempt to reconnect.
     */
    public void ensureConnected() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Reconnecting to database...");
            connectToDatabase();
        }
    }

    /**
     * Closes the database connection and statement.
     */
    public void closeConnection() {
        try { 
            if (statement != null) {
                statement.close(); 
            }
        } catch (SQLException se2) { 
            se2.printStackTrace();
        } 
        try { 
            if (connection != null) {
                connection.close(); 
            }
        } catch (SQLException se) { 
            se.printStackTrace(); 
        } 
    }
    
    //================================================================================
    // Review Related Methods
    //================================================================================
    
    /**
     * Inserts a new review into the database.
     * If answerID is 0, it means the review is for a question.
     * 
     * @param questionID The ID of the question
     * @param answerID The ID of the answer (0 if the review is for a question)
     * @param reviewerName The name of the reviewer
     * @param reviewText The text of the review
     * @param dateCreated The date the review was created
     */
    public void insertReview(int questionID, int answerID, String reviewerName, 
                          String reviewText, java.util.Date dateCreated) throws SQLException {
        ensureConnected();
        String query = "INSERT INTO Reviews (questionID, answerID, reviewerName, reviewText, dateCreated) "
                     + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionID);
            pstmt.setInt(2, answerID);
            pstmt.setString(3, reviewerName);
            pstmt.setString(4, reviewText);
            pstmt.setTimestamp(5, new Timestamp(dateCreated.getTime()));
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Retrieves all reviews from the database.
     * Returns an array of String arrays, where each String array contains:
     * [0] = Type (Question or Answer)
     * [1] = ID (Question ID or Answer ID)
     * [2] = Content (Question text or Answer text)
     * [3] = Review text
     * [4] = Reviewer name
     * [5] = Date created
     */
    public List<String[]> getAllReviews() throws SQLException {
        ensureConnected();
        List<String[]> reviewList = new ArrayList<>();
        
        String query = "SELECT r.reviewID, r.questionID, r.answerID, r.reviewerName, r.reviewText, r.dateCreated, " +
                      "q.bodyText AS questionText, a.bodyText AS answerText " +
                      "FROM Reviews r " +
                      "JOIN Questions q ON r.questionID = q.questionID " +
                      "LEFT JOIN Answers a ON r.answerID = a.answerID " +
                      "ORDER BY r.dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int answerID = rs.getInt("answerID");
                String type = (answerID == 0) ? "Question" : "Answer";
                String id = (answerID == 0) ? 
                        String.valueOf(rs.getInt("questionID")) : 
                        String.valueOf(rs.getInt("answerID"));
                String content = (answerID == 0) ? 
                        rs.getString("questionText") : 
                        rs.getString("answerText");
                
                String reviewText = rs.getString("reviewText");
                String reviewerName = rs.getString("reviewerName");
                String dateCreated = rs.getTimestamp("dateCreated").toString();
                
                reviewList.add(new String[]{type, id, content, reviewText, reviewerName, dateCreated});
            }
        }
        
        return reviewList;
    }
    
    /**
     * Retrieves all reviews from the database with their review IDs included.
     * Returns an array of String arrays, where each String array contains:
     * [0] = Review ID
     * [1] = Type (Question or Answer)
     * [2] = ID (Question ID or Answer ID)
     * [3] = Content (Question text or Answer text)
     * [4] = Review text
     * [5] = Reviewer name
     * [6] = Date created
     */
    public List<String[]> getAllReviewsWithIDs() throws SQLException {
        ensureConnected();
        List<String[]> reviewList = new ArrayList<>();
        
        String query = "SELECT r.reviewID, r.questionID, r.answerID, r.reviewerName, r.reviewText, r.dateCreated, " +
                      "q.bodyText AS questionText, a.bodyText AS answerText " +
                      "FROM Reviews r " +
                      "JOIN Questions q ON r.questionID = q.questionID " +
                      "LEFT JOIN Answers a ON r.answerID = a.answerID AND r.answerID > 0 " +
                      "ORDER BY r.dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String reviewID = String.valueOf(rs.getInt("reviewID"));
                int answerID = rs.getInt("answerID");
                String type = (answerID == 0) ? "Question" : "Answer";
                String id = (answerID == 0) ? 
                        String.valueOf(rs.getInt("questionID")) : 
                        String.valueOf(rs.getInt("answerID"));
                String content = (answerID == 0) ? 
                        rs.getString("questionText") : 
                        rs.getString("answerText");
                
                String reviewText = rs.getString("reviewText");
                String reviewerName = rs.getString("reviewerName");
                String dateCreated = rs.getTimestamp("dateCreated").toString();
                
                reviewList.add(new String[]{reviewID, type, id, content, reviewText, reviewerName, dateCreated});
            }
        }
        
        return reviewList;
    }
    
    /**
     * Retrieves all question reviews from the database with their review IDs included.
     * Returns an array of String arrays, where each String array contains:
     * [0] = Review ID
     * [1] = Question ID
     * [2] = Question text
     * [3] = Review text
     * [4] = Reviewer name
     * [5] = Date created
     */
    public List<String[]> getQuestionReviewsWithIDs() throws SQLException {
        ensureConnected();
        List<String[]> reviewList = new ArrayList<>();
        
        String query = "SELECT r.reviewID, r.questionID, r.reviewerName, r.reviewText, r.dateCreated, " +
                      "q.bodyText AS questionText " +
                      "FROM Reviews r " +
                      "JOIN Questions q ON r.questionID = q.questionID " +
                      "WHERE r.answerID = 0 " +
                      "ORDER BY r.dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String reviewID = String.valueOf(rs.getInt("reviewID"));
                String questionID = String.valueOf(rs.getInt("questionID"));
                String questionText = rs.getString("questionText");
                String reviewText = rs.getString("reviewText");
                String reviewerName = rs.getString("reviewerName");
                String dateCreated = rs.getTimestamp("dateCreated").toString();
                
                reviewList.add(new String[]{reviewID, questionID, questionText, reviewText, reviewerName, dateCreated});
            }
        }
        
        return reviewList;
    }
    
    /**
     * Retrieves all answer reviews from the database with their review IDs included.
     * Returns an array of String arrays, where each String array contains:
     * [0] = Review ID
     * [1] = Answer ID
     * [2] = Answer text
     * [3] = Review text
     * [4] = Reviewer name
     * [5] = Date created
     */
    public List<String[]> getAnswerReviewsWithIDs() throws SQLException {
        ensureConnected();
        List<String[]> reviewList = new ArrayList<>();
        
        String query = "SELECT r.reviewID, r.answerID, r.reviewerName, r.reviewText, r.dateCreated, " +
                      "a.bodyText AS answerText " +
                      "FROM Reviews r " +
                      "JOIN Answers a ON r.answerID = a.answerID " +
                      "WHERE r.answerID > 0 " +
                      "ORDER BY r.dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String reviewID = String.valueOf(rs.getInt("reviewID"));
                String answerID = String.valueOf(rs.getInt("answerID"));
                String answerText = rs.getString("answerText");
                String reviewText = rs.getString("reviewText");
                String reviewerName = rs.getString("reviewerName");
                String dateCreated = rs.getTimestamp("dateCreated").toString();
                
                reviewList.add(new String[]{reviewID, answerID, answerText, reviewText, reviewerName, dateCreated});
            }
        }
        
        return reviewList;
    }
    
    /**
     * Updates a review with new text.
     * 
     * @param reviewID The ID of the review to update
     * @param newReviewText The new text for the review
     */
    public void updateReview(int reviewID, String newReviewText) throws SQLException {
        ensureConnected();
        String query = "UPDATE Reviews SET reviewText = ? WHERE reviewID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newReviewText);
            pstmt.setInt(2, reviewID);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Deletes a review.
     * 
     * @param reviewID The ID of the review to delete
     */
    public void deleteReview(int reviewID) throws SQLException {
        ensureConnected();
        String query = "DELETE FROM Reviews WHERE reviewID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewID);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Checks if a user is the owner of a specific review.
     * 
     * @param reviewID The ID of the review
     * @param userName The username to check
     * @return true if the user is the owner, false otherwise
     */
    public boolean isReviewOwner(int reviewID, String userName) throws SQLException {
        ensureConnected();
        String query = "SELECT reviewerName FROM Reviews WHERE reviewID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String reviewerName = rs.getString("reviewerName");
                    return reviewerName.equals(userName);
                }
                return false;
            }
        }
    }

    /**
     * Gets all reviews for a specific question including review IDs.
     * 
     * @param questionID The ID of the question
     * @return A list of review information arrays
     */
    public List<String[]> getReviewsForQuestionWithIDs(int questionID) throws SQLException {
        ensureConnected();
        List<String[]> reviewList = new ArrayList<>();
        
        String query = "SELECT r.reviewID, r.questionID, r.reviewerName, r.reviewText, r.dateCreated, " +
                      "q.bodyText AS questionText " +
                      "FROM Reviews r " +
                      "JOIN Questions q ON r.questionID = q.questionID " +
                      "WHERE r.questionID = ? AND r.answerID = 0 " +
                      "ORDER BY r.dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String reviewID = String.valueOf(rs.getInt("reviewID"));
                String qID = String.valueOf(rs.getInt("questionID"));
                String questionText = rs.getString("questionText");
                String reviewText = rs.getString("reviewText");
                String reviewerName = rs.getString("reviewerName");
                String dateCreated = rs.getTimestamp("dateCreated").toString();
                
                reviewList.add(new String[]{reviewID, qID, questionText, reviewText, reviewerName, dateCreated});
            }
        }
        
        return reviewList;
    }
    
    /**
     * Gets all reviews for a specific answer including review IDs.
     * 
     * @param answerID The ID of the answer
     * @return A list of review information arrays
     */
    public List<String[]> getReviewsForAnswerWithIDs(int answerID) throws SQLException {
        ensureConnected();
        List<String[]> reviewList = new ArrayList<>();
        
        String query = "SELECT r.reviewID, r.answerID, r.reviewerName, r.reviewText, r.dateCreated, " +
                      "a.bodyText AS answerText " +
                      "FROM Reviews r " +
                      "JOIN Answers a ON r.answerID = a.answerID " +
                      "WHERE r.answerID = ? " +
                      "ORDER BY r.dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String reviewID = String.valueOf(rs.getInt("reviewID"));
                String aID = String.valueOf(rs.getInt("answerID"));
                String answerText = rs.getString("answerText");
                String reviewText = rs.getString("reviewText");
                String reviewerName = rs.getString("reviewerName");
                String dateCreated = rs.getTimestamp("dateCreated").toString();
                
                reviewList.add(new String[]{reviewID, aID, answerText, reviewText, reviewerName, dateCreated});
            }
        }
        
        return reviewList;
    }

    /**
     * Gets all reviews for a specific question.
     */
    public List<String[]> getReviewsForQuestion(int questionID) throws SQLException {
        ensureConnected();
        List<String[]> reviewList = new ArrayList<>();
        
        String query = "SELECT r.reviewID, r.answerID, r.reviewerName, r.reviewText, r.dateCreated " +
                      "FROM Reviews r " +
                      "WHERE r.questionID = ? AND r.answerID = 0 " +
                      "ORDER BY r.dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String reviewID = String.valueOf(rs.getInt("reviewID"));
                String reviewerName = rs.getString("reviewerName");
                String reviewText = rs.getString("reviewText");
                String dateCreated = rs.getTimestamp("dateCreated").toString();
                
                reviewList.add(new String[]{reviewID, reviewerName, reviewText, dateCreated});
            }
        }
        
        return reviewList;
    }
    
    /**
     * Gets all reviews for a specific answer.
     */
    public List<String[]> getReviewsForAnswer(int answerID) throws SQLException {
        ensureConnected();
        List<String[]> reviewList = new ArrayList<>();
        
        String query = "SELECT r.reviewID, r.questionID, r.reviewerName, r.reviewText, r.dateCreated " +
                      "FROM Reviews r " +
                      "WHERE r.answerID = ? " +
                      "ORDER BY r.dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String reviewID = String.valueOf(rs.getInt("reviewID"));
                String reviewerName = rs.getString("reviewerName");
                String reviewText = rs.getString("reviewText");
                String dateCreated = rs.getTimestamp("dateCreated").toString();
                
                reviewList.add(new String[]{reviewID, reviewerName, reviewText, dateCreated});
            }
        }
        
        return reviewList;
    }
    
    public List<String[]> getReviewsByReviewer(String reviewerName) throws SQLException {
        ensureConnected();
        List<String[]> result = new ArrayList<>();

        String sql = "SELECT r.answerID, r.questionID, r.reviewText, r.dateCreated, r.reviewerName, " +
                     "q.bodyText AS questionText, a.bodyText AS answerText " +
                     "FROM Reviews r " +
                     "LEFT JOIN Questions q ON r.questionID = q.questionID " +
                     "LEFT JOIN Answers a ON r.answerID = a.answerID " +
                     "WHERE r.reviewerName = ? " +
                     "ORDER BY r.dateCreated DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reviewerName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boolean isAnswer = rs.getInt("answerID") != 0;
                String type = isAnswer ? "Answer" : "Question";
                String targetID = isAnswer ? rs.getString("answerID") : rs.getString("questionID");
                String content = isAnswer ? rs.getString("answerText") : rs.getString("questionText");
                String reviewText = rs.getString("reviewText");
                String date = rs.getTimestamp("dateCreated").toString();

                result.add(new String[]{type, targetID, content, reviewText, date});
            }
        }

        return result;
    }
    
    public String getReviewerForQuestion(int questionID) throws SQLException {
        ensureConnected();
        String sql = "SELECT reviewerName FROM Reviews WHERE questionID = ? AND answerID = 0 LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("reviewerName");
            } else {
                // Instead of throwing an exception, return a default value
                return "NoReviewer";
            }
        }
    }

    //================================================================================
    // Feedback Related Methods
    //================================================================================

    /**
     * Inserts feedback entry into the database for a specific question.
     */
    public void insertFeedback(int questionID, String sentTo, String sentBy, String feedbackText) throws SQLException {
        ensureConnected();
        String query = "INSERT INTO Feedback (questionID, sentTo, sentBy, feedbackText) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionID);
            pstmt.setString(2, sentTo);
            pstmt.setString(3, sentBy);
            pstmt.setString(4, feedbackText);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all feedback and replies for a specific user.
     */
    public List<String[]> getFeedbackForUser(String username) throws SQLException {
        ensureConnected();
        List<String[]> feedbackList = new ArrayList<>();

        String query = "SELECT f.id, f.feedbackText, f.sentBy, q.bodyText, f.questionID, f.parentID, f.timestamp, "
                     + "CASE WHEN f.parentID IS NULL THEN 'Feedback' ELSE 'Reply' END AS type "
                     + "FROM Feedback f "
                     + "JOIN Questions q ON f.questionID = q.questionID "
                     + "WHERE f.sentTo = ? "
                     + "ORDER BY f.id DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String feedbackID = String.valueOf(rs.getInt("id"));
                String questionID = String.valueOf(rs.getInt("questionID"));
                String questionText = rs.getString("bodyText");  
                String feedbackText = rs.getString("feedbackText");
                String sentBy = rs.getString("sentBy");
                String dateTime = rs.getTimestamp("timestamp").toString();
                String type = rs.getString("type");

                feedbackList.add(new String[]{type, questionID, feedbackID, questionText, feedbackText, sentBy, dateTime});
            }
        }
        return feedbackList;
    }
    
    /**
     * Inserts a reply to an existing feedback entry.
     */
    public void insertReply(int parentID, String sentTo, String sentBy, String replyText) throws SQLException {
        ensureConnected();
        
        String getQuestionQuery = "SELECT questionID FROM Feedback WHERE id = ?";
        int questionID = -1;

        try (PreparedStatement pstmt = connection.prepareStatement(getQuestionQuery)) {
            pstmt.setInt(1, parentID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                questionID = rs.getInt("questionID");
            }
        }

        if (questionID == -1) {
            throw new SQLException("Error: Unable to retrieve questionID for reply.");
        }

        String insertReplyQuery = "INSERT INTO Feedback (parentID, questionID, sentTo, sentBy, feedbackText) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertReplyQuery)) {
            pstmt.setInt(1, parentID);
            pstmt.setInt(2, questionID);
            pstmt.setString(3, sentTo);
            pstmt.setString(4, sentBy);
            pstmt.setString(5, replyText);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Inserts a feedback about review
     */
    public void insertReviewFeedback(int reviewID, int targetID, boolean isAnswer, String sender, String receiver, String message) throws SQLException {
        ensureConnected();
        String query = "INSERT INTO ReviewFeedback (reviewID, targetID, isAnswer, sentTo, sentBy, feedbackText) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewID);
            pstmt.setInt(2, targetID);
            pstmt.setBoolean(3, isAnswer);
            pstmt.setString(4, receiver);
            pstmt.setString(5, sender);
            pstmt.setString(6, message);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Inserts review feedback for a reviewer by auto-resolving reviewID from reviewer name.
     * Feedback is linked to the most recent review made by that reviewer.
     */
    public void insertFeedbackForReviewer(String reviewerName, String sender, String message) throws SQLException {
        ensureConnected();

        String getReviewIDQuery = "SELECT reviewID FROM Reviews WHERE reviewerName = ? ORDER BY dateCreated DESC LIMIT 1";
        int reviewID = -1;

        try (PreparedStatement pstmt = connection.prepareStatement(getReviewIDQuery)) {
            pstmt.setString(1, reviewerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                reviewID = rs.getInt("reviewID");
            } else {
                throw new SQLException("No review found for reviewer: " + reviewerName);
            }
        }

        String insertQuery = "INSERT INTO ReviewFeedback (reviewID, targetID, isAnswer, sentTo, sentBy, feedbackText) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setInt(1, reviewID);
            pstmt.setInt(2, 0); // placeholder target ID
            pstmt.setBoolean(3, false); // assuming it's for question
            pstmt.setString(4, reviewerName);
            pstmt.setString(5, sender);
            pstmt.setString(6, message);
            pstmt.executeUpdate();
        }
    }

    
    /**
     * Inserts a reply about review feedback
     */
    public void insertReviewReply(int parentID, int reviewID, int targetID, boolean isAnswer, String sender, String receiver, String message) throws SQLException {
        ensureConnected();
        String query = "INSERT INTO ReviewFeedback (reviewID, targetID, isAnswer, parentID, sentTo, sentBy, feedbackText) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewID);
            pstmt.setInt(2, targetID);
            pstmt.setBoolean(3, isAnswer);
            pstmt.setInt(4, parentID);
            pstmt.setString(5, receiver);
            pstmt.setString(6, sender);
            pstmt.setString(7, message);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Returns Table info for feedback inbox
     */
    public List<String[]> getReviewFeedbackForReviewer(String reviewerName) throws SQLException {
        ensureConnected();
        List<String[]> result = new ArrayList<>();

        String query = "SELECT rf.id, rf.reviewID, rf.targetID, rf.isAnswer, rf.feedbackText, " +
                       "rf.sentBy, rf.timestamp, rf.parentID, " +
                       "q.bodyText AS questionText, a.bodyText AS answerText " +
                       "FROM ReviewFeedback rf " +
                       "LEFT JOIN Questions q ON rf.targetID = q.questionID AND rf.isAnswer = false " +
                       "LEFT JOIN Answers a ON rf.targetID = a.answerID AND rf.isAnswer = true " +
                       "WHERE rf.sentTo = ? " +
                       "ORDER BY rf.timestamp DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, reviewerName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = rs.getObject("parentID") == null ? "Feedback" : "Reply";
                boolean isAnswer = rs.getBoolean("isAnswer");

                String content = isAnswer ? rs.getString("answerText") : rs.getString("questionText");
                String targetType = isAnswer ? "Answer" : "Question";
                String targetDisplay = targetType + ": " + (content != null ? content : "[Deleted]");

                result.add(new String[]{
                    type,                      // 0 - Feedback or Reply
                    targetDisplay,            // 1 - Question or Answer content
                    String.valueOf(rs.getInt("id")),     // 2 - Feedback ID
                    rs.getString("feedbackText"),        // 3 - Message
                    rs.getString("sentBy"),              // 4 - From
                    rs.getTimestamp("timestamp").toString(), // 5 - Date
                    String.valueOf(rs.getInt("reviewID")),    // 6 - Review ID
                    String.valueOf(rs.getInt("targetID")),    // 7 - Target ID
                    String.valueOf(isAnswer)             // 8 - isAnswer flag
                });
            }
        }

        return result;
    }

    //================================================================================
    // Chat Related Methods
    //================================================================================
    
    public void insertChatMessage(String role, String senderUsername, int questionID, String message) throws SQLException {
        ensureConnected();
        String reviewerUsername;
        
        try {
            reviewerUsername = getReviewerForQuestion(questionID);
        } catch (SQLException e) {
            // If no reviewer found, use a default value
            reviewerUsername = "NoReviewer";
        }

        String sql = "INSERT INTO ChatMessages (question_id, reviewer_username, student_username, sender_role, message) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionID);
            stmt.setString(2, reviewerUsername);
            
            // If the sender is a student, use their username as student_username
            // Otherwise, get the student username by finding who posted the question
            String studentUsername;
            if (role.equals("Student")) {
                studentUsername = senderUsername;
            } else {
                // For staff/instructor, find the student who posted the question
                String query = "SELECT postedBy FROM Questions WHERE questionID = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setInt(1, questionID);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        studentUsername = rs.getString("postedBy");
                    } else {
                        throw new SQLException("Question not found: " + questionID);
                    }
                }
            }
            
            stmt.setString(3, studentUsername);
            stmt.setString(4, role);
            stmt.setString(5, message);
            stmt.executeUpdate();
        }
    }

    public List<String> getChatMessagesForQuestion(String studentUsername, int questionID) throws SQLException {
        ensureConnected();
        String reviewerUsername;
        
        try {
            reviewerUsername = getReviewerForQuestion(questionID);
        } catch (SQLException e) {
            // If no reviewer found, use a default value
            reviewerUsername = "NoReviewer";
        }

        String sql = "SELECT sender_role, message, timestamp FROM ChatMessages " +
                     "WHERE question_id = ? AND student_username = ? " +
                     "ORDER BY timestamp ASC";

        List<String> messages = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionID);
            stmt.setString(2, studentUsername);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String role = rs.getString("sender_role");
                String msg = rs.getString("message");
                String time = rs.getString("timestamp");
                messages.add(role + ": " + msg + " (" + time + ")");
            }
        }
        return messages;
    }

    public List<String> getChatBetweenUsers(String user1, String user2) throws SQLException {
        ensureConnected();
        List<String> messages = new ArrayList<>();
        String query = "SELECT sender_role, message, timestamp FROM ChatMessages " +
                "WHERE ((student_username = ? AND reviewer_username = ?) " +
                "OR (student_username = ? AND reviewer_username = ?)) " +
                "AND question_id = -1 " +
                "ORDER BY timestamp ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String sender = rs.getString("sender_role");
                String message = rs.getString("message");
                String timestamp = rs.getTimestamp("timestamp").toString();

                messages.add(sender + " (" + timestamp + "): " + message);
            }
        }
        return messages;
    }
    
    public void insertGeneralChatMessage(String role, String studentUsername, String reviewerUsername, String message) throws SQLException {
        ensureConnected();
        String sql = "INSERT INTO ChatMessages (question_id, reviewer_username, student_username, sender_role, message) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, -1); // -1 to indicate general chat
            stmt.setString(2, reviewerUsername);
            stmt.setString(3, studentUsername);
            stmt.setString(4, role);
            stmt.setString(5, message);
            stmt.executeUpdate();
        }
    }
    
    public List<String[]> getChatMessagesForReviewer(String reviewerName) throws SQLException {
        ensureConnected();
        List<String[]> result = new ArrayList<>();

        String query = "SELECT message_id, student_username, sender_role, message, timestamp " +
                      "FROM ChatMessages " +
                      "WHERE reviewer_username = ? AND question_id = -1 " +
                      "AND sender_role = 'Student' " +  // Only show messages from students
                      "ORDER BY timestamp DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, reviewerName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = "Chat";
                String content = "Chat with " + rs.getString("student_username");
                String messageId = String.valueOf(rs.getInt("message_id"));
                String message = rs.getString("message");
                String sender = rs.getString("student_username");
                String timestamp = rs.getTimestamp("timestamp").toString();
                String senderRole = rs.getString("sender_role");

                result.add(new String[]{
                    type,                      // 0 - Type (Chat)
                    content,                   // 1 - Content (Chat with student)
                    messageId,                 // 2 - Message ID
                    message,                   // 3 - Message
                    sender,                    // 4 - From
                    timestamp,                 // 5 - Date
                    "-1",                      // 6 - Review ID (not applicable for chats)
                    "-1",                      // 7 - Target ID (not applicable for chats)
                    "false"                    // 8 - isAnswer flag (not applicable for chats)
                });
            }
        }

        return result;
    }
    
    /**
     * Retrieves general chat messages for a user (outside of question contexts)
     * 
     * @param username The username to get chat messages for
     * @return List of chat messages with sender info
     */
    public List<String[]> getGeneralChatMessages(String username) throws SQLException {
        ensureConnected();
        List<String[]> result = new ArrayList<>();

        String query = "SELECT c.message_id, " +
               "CASE WHEN c.student_username = ? THEN c.reviewer_username ELSE c.student_username END AS other_user, " +
               "c.sender_role, c.message, c.timestamp " +
               "FROM ChatMessages c " +
               "WHERE (c.student_username = ? OR c.reviewer_username = ?) " +
               "AND c.question_id = -1 " +
               "ORDER BY c.timestamp DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String messageId = String.valueOf(rs.getInt("message_id"));
                String otherUser = rs.getString("other_user");
                String senderRole = rs.getString("sender_role");
                String message = rs.getString("message");
                String timestamp = rs.getTimestamp("timestamp").toString();

                // Format: 
                // 0: message ID
                // 1: other user in the conversation
                // 2: sender (username)
                // 3: message content
                // 4: timestamp
                // 5: sender role
                result.add(new String[]{
                    messageId,
                    otherUser,
                    username,
                    message,
                    timestamp,
                    senderRole
                });
            }
        }

        return result;
    }
    
    //================================================================================
    // Reviewer Weight Related Methods
    //================================================================================
    
    /**
     * Sets or updates the weight for a reviewer from a student's perspective
     */
    public void setReviewerWeight(String studentUsername, String reviewerUsername, int weight) throws SQLException {
        ensureConnected();
        // Explicit check for negative weights
        if (weight < 0) {
            System.out.println("Attempting to set negative weight: " + weight);
            throw new SQLException("Weight cannot be negative");
        }
        String query = "MERGE INTO ReviewerWeights (studentUsername, reviewerUsername, weight) "
                     + "KEY (studentUsername, reviewerUsername) "
                     + "VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, reviewerUsername);
            pstmt.setInt(3, weight);
            pstmt.executeUpdate();
        }
    }

    /**
     * Gets the weight for a reviewer from a student's perspective
     */
    public int getReviewerWeight(String studentUsername, String reviewerUsername) throws SQLException {
        ensureConnected();
        String query = "SELECT weight FROM ReviewerWeights WHERE studentUsername = ? AND reviewerUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, reviewerUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("weight");
            }
            return 0; // Default weight if not set
        }
    }

    /**
     * Gets all reviewer weights for a student
     */
    public List<String[]> getReviewerWeights(String studentUsername) throws SQLException {
        ensureConnected();
        List<String[]> weights = new ArrayList<>();
        String query = "SELECT reviewerUsername, weight FROM ReviewerWeights WHERE studentUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                weights.add(new String[]{
                    rs.getString("reviewerUsername"),
                    String.valueOf(rs.getInt("weight"))
                });
            }
        }
        return weights;
    }
    
    //================================================================================
    // Flagged Content Methods
    //================================================================================
    
    /**
     * Flags content for review by instructors or staff.
     * 
     * @param contentType The type of content being flagged ('Question', 'Answer', or 'Feedback')
     * @param contentID The ID of the content being flagged
     * @param flaggedBy The username of the person who flagged the content
     * @param description Optional description or reason for flagging
     */
    public void flagContent(String contentType, int contentID, String flaggedBy, String description) throws SQLException {
        ensureConnected();
        String query = "INSERT INTO FlaggedContent (contentType, contentID, flaggedBy, description) "
                      + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, contentType);
            pstmt.setInt(2, contentID);
            pstmt.setString(3, flaggedBy);
            pstmt.setString(4, description);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Retrieves all flagged content from the database.
     * 
     * @return A list of String arrays containing information about flagged content
     */
    public List<String[]> getAllFlaggedContent() throws SQLException {
        ensureConnected();
        List<String[]> flaggedContent = new ArrayList<>();
        
        String query = "SELECT fc.id, fc.contentType, fc.contentID, fc.flaggedBy, fc.flaggedAt, fc.description, " +
                      "CASE " +
                      "  WHEN fc.contentType = 'Question' THEN (SELECT bodyText FROM Questions WHERE questionID = fc.contentID) " +
                      "  WHEN fc.contentType = 'Answer' THEN (SELECT bodyText FROM Answers WHERE answerID = fc.contentID) " +
                      "  WHEN fc.contentType = 'Feedback' THEN (SELECT feedbackText FROM Feedback WHERE id = fc.contentID) " +
                      "END as contentText " +
                      "FROM FlaggedContent fc " +
                      "WHERE fc.resolved = false " +
                      "ORDER BY fc.flaggedAt DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String[] content = new String[7];
                content[0] = rs.getString("id");
                content[1] = rs.getString("contentType");
                content[2] = rs.getString("contentID");
                content[3] = rs.getString("flaggedBy");
                content[4] = rs.getString("flaggedAt");
                content[5] = rs.getString("description");
                content[6] = rs.getString("contentText");
                flaggedContent.add(content);
            }
        }
        
        return flaggedContent;
    }
    
    /**
     * Marks a flagged content as resolved.
     * 
     * @param flagID The ID of the flagged content
     */
    public void resolveFlaggedContent(int flagID) throws SQLException {
        ensureConnected();
        String query = "UPDATE FlaggedContent SET resolved = true WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, flagID);
            pstmt.executeUpdate();
        }
    }

    //================================================================================
    // Question Search Methods
    //================================================================================
    
    /**
     * Searches for questions with filtering options.
     * 
     * @param keyword The keyword to search for in question text
     * @param filterType The type of filter to apply ("All", "Answered", "Unanswered", "Reviewer")
     * @param filterValue Additional filter value (reviewer username if filterType is "Reviewer")
     * @return A list of questions matching the search criteria
     */
    public List<Question> searchQuestions(String keyword, String filterType, String filterValue) throws SQLException {
        ensureConnected();
        List<Question> results = new ArrayList<>();
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT q.* FROM Questions q ");
        
        // Add joins based on filter type
        if (filterType.equals("Reviewer")) {
            queryBuilder.append("JOIN Reviews r ON q.questionID = r.questionID AND r.answerID = 0 ");
        } else if (filterType.equals("Answered")) {
            queryBuilder.append("JOIN Answers a ON q.questionID = a.questionID ");
        } else if (filterType.equals("Unanswered")) {
            queryBuilder.append("LEFT JOIN Answers a ON q.questionID = a.questionID ");
        }
        
        // Start building WHERE clause
        queryBuilder.append("WHERE 1=1 ");
        
        // Add keyword search condition
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryBuilder.append("AND LOWER(q.bodyText) LIKE ? ");
        }
        
        // Add filter conditions
        if (filterType.equals("Answered")) {
            // Questions that have at least one answer (join already ensures this)
        } else if (filterType.equals("Unanswered")) {
            queryBuilder.append("AND a.answerID IS NULL "); // Questions with no answers
        } else if (filterType.equals("Reviewer") && filterValue != null && !filterValue.trim().isEmpty()) {
            queryBuilder.append("AND r.reviewerName = ? ");
        }
        
        queryBuilder.append("ORDER BY q.dateCreated DESC");
        
        try (PreparedStatement pstmt = connection.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            
            // Set keyword parameter
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword.toLowerCase() + "%");
            }
            
            // Set reviewer parameter if needed
            if (filterType.equals("Reviewer") && filterValue != null && !filterValue.trim().isEmpty()) {
                pstmt.setString(paramIndex, filterValue);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Question question = new Question(
                    rs.getInt("questionID"),
                    rs.getString("bodyText"),
                    rs.getString("postedBy"),
                    new java.util.Date(rs.getTimestamp("dateCreated").getTime())
                );
                question.setResolved(rs.getBoolean("resolvedStatus"));
                question.setAcceptedAnsID(rs.getInt("acceptedAnsID"));
                question.setNewMessagesCount(rs.getInt("newMessagesCount"));
                results.add(question);
            }
        }
        
        return results;
    }
    
    /**
     * Gets a list of all reviewers who have reviewed questions.
     * 
     * @return A list of reviewer usernames
     */
    public List<String> getAllReviewers() throws SQLException {
        ensureConnected();
        List<String> reviewers = new ArrayList<>();
        
        String query = "SELECT DISTINCT reviewerName FROM Reviews WHERE answerID = 0 ORDER BY reviewerName";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                reviewers.add(rs.getString("reviewerName"));
            }
        }
        
        return reviewers;
    }
    
    /**
     * Retrieves all questions posted by a specific user.
     * 
     * @param username The username to get questions for
     * @return A list of questions posted by the user
     */
    public List<Question> getQuestionsByUser(String username) throws SQLException {
        ensureConnected();
        List<Question> results = new ArrayList<>();
        
        String query = "SELECT * FROM Questions WHERE postedBy = ? ORDER BY dateCreated DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Question question = new Question(
                    rs.getInt("questionID"),
                    rs.getString("bodyText"),
                    rs.getString("postedBy"),
                    new java.util.Date(rs.getTimestamp("dateCreated").getTime())
                );
                question.setResolved(rs.getBoolean("resolvedStatus"));
                question.setAcceptedAnsID(rs.getInt("acceptedAnsID"));
                question.setNewMessagesCount(rs.getInt("newMessagesCount"));
                results.add(question);
            }
        }
        
        return results;
    }

    //================================================================================
    // Reviewer Profile Methods
    //================================================================================

    /**
     * Creates or updates a reviewer's profile.
     */
    public void updateReviewerProfile(String userName, String experience, String background) throws SQLException {
        ensureConnected();
        
        // First, create the table if it doesn't exist
        statement.execute("CREATE TABLE IF NOT EXISTS ReviewerProfiles (" +
            "userName VARCHAR(50) PRIMARY KEY," +
            "experience TEXT," +
            "background TEXT," +
            "lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        // Then perform the merge operation
        String query = "MERGE INTO ReviewerProfiles (userName, experience, background, lastUpdated) " +
                      "KEY (userName) " +
                      "VALUES (?, ?, ?, CURRENT_TIMESTAMP())";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, experience);
            pstmt.setString(3, background);
            pstmt.executeUpdate();
        }
    }

    
    /**
     * Retrieves a reviewer's profile.
     */
    public ReviewerProfile getReviewerProfile(String userName) throws SQLException {
        ensureConnected();
        
        // First, create the table if it doesn't exist
        statement.execute("CREATE TABLE IF NOT EXISTS ReviewerProfiles (" +
            "userName VARCHAR(50) PRIMARY KEY," +
            "experience TEXT," +
            "background TEXT," +
            "lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        String query = "SELECT rp.*, " +
                      "(SELECT COUNT(*) FROM Reviews r WHERE r.reviewerName = ?) as totalReviews, " +
                      "(SELECT COALESCE(AVG(CAST(weight as DOUBLE)), 0.0) FROM ReviewerWeights rw WHERE rw.reviewerUsername = ?) as avgRating " +
                      "FROM ReviewerProfiles rp " +
                      "WHERE rp.userName = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, userName);
            pstmt.setString(3, userName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                ReviewerProfile profile = new ReviewerProfile(
                    userName,
                    rs.getString("experience"),
                    rs.getString("background")
                );
                profile.setTotalReviews(rs.getInt("totalReviews"));
                profile.setAverageRating(rs.getDouble("avgRating"));
                return profile;
            }
            // If no profile exists, create a new empty one
            return new ReviewerProfile(userName, "", "");
        }
    }

    /**
     * Gets all feedback received for a reviewer.
     */
    public List<String[]> getReviewerFeedback(String reviewerName) throws SQLException {
        ensureConnected();
        List<String[]> feedback = new ArrayList<>();
        
        String query = "SELECT rf.feedbackText, rf.sentBy, rf.timestamp, " +
                      "CASE WHEN rf.isAnswer THEN 'Answer Review' ELSE 'Question Review' END as reviewType " +
                      "FROM ReviewFeedback rf " +
                      "WHERE rf.sentTo = ? AND rf.parentID IS NULL " +
                      "ORDER BY rf.timestamp DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, reviewerName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                feedback.add(new String[]{
                    rs.getString("reviewType"),
                    rs.getString("feedbackText"),
                    rs.getString("sentBy"),
                    rs.getTimestamp("timestamp").toString()
                });
            }
        }
        return feedback;
    }
}