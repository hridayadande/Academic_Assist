package databasePart1;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import application.Question;
import application.Answer;

/**
 * The DatabaseHelper2 class is responsible for managing question and answer operations
 * in the database. This class handles database setup and QA related database interactions.
 */
public class DatabaseHelper2 {

    // JDBC driver name and database URL 
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

    // Database credentials 
    static final String USER = "sa"; 
    static final String PASS = ""; 

    private Connection connection = null;
    private Statement statement = null; 
 
    /**
     * Connects to the database and creates the necessary tables.
     */
    public void connectToDatabase() throws SQLException {
        try {
            // Load the JDBC driver
            Class.forName(JDBC_DRIVER); 
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 
            
            createTables();  // Create the necessary tables if they don't exist
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
     * Creates the necessary tables if they do not exist.
     */
    private void createTables() throws SQLException {
    	// Create Questions table
        String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
                + "questionID INT PRIMARY KEY, "
                + "bodyText TEXT, "
                + "postedBy VARCHAR(255), "
                + "dateCreated TIMESTAMP, "
                + "resolvedStatus BOOLEAN DEFAULT FALSE, "
                + "acceptedAnsID INT DEFAULT -1, "
                + "newMessagesCount INT DEFAULT 0)";
        statement.execute(questionsTable);

        // Create Answers table with consistent column naming
        String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
                + "answerID INT PRIMARY KEY, "
                + "questionID INT, "
                + "bodyText TEXT, "
                + "answeredBy VARCHAR(255), "
                + "dateCreated TIMESTAMP, "
                + "FOREIGN KEY (questionID) REFERENCES Questions(questionID))";
        statement.execute(answersTable);
        
        //Create a table to maintain feedback
        String feedbackTable = "CREATE TABLE IF NOT EXISTS Feedback ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionID INT, "
                + "sentTo VARCHAR(255), "
                + "sentBy VARCHAR(255), "
                + "feedbackText TEXT, "
                + "parentID INT DEFAULT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (questionID) REFERENCES Questions(questionID) ON DELETE CASCADE, "
                + "FOREIGN KEY (parentID) REFERENCES Feedback(id) ON DELETE CASCADE"
                + ")";
        statement.execute(feedbackTable);

        // Create a table for reviews
        String reviewsTable = "CREATE TABLE IF NOT EXISTS Reviews ("
                + "reviewID INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionID INT, "
                + "answerID INT DEFAULT 0, "
                + "reviewerName VARCHAR(255), "
                + "reviewText TEXT, "
                + "dateCreated TIMESTAMP, "
                + "FOREIGN KEY (questionID) REFERENCES Questions(questionID) ON DELETE CASCADE"
                + ")";
        statement.execute(reviewsTable);

        // Create a table for  reviews feedback
        String reviewFeedbackTable = "CREATE TABLE IF NOT EXISTS ReviewFeedback ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "reviewID INT, "
                + "targetID INT, "
                + "isAnswer BOOLEAN, "
                + "sentTo VARCHAR(255), "
                + "sentBy VARCHAR(255), "
                + "feedbackText TEXT, "
                + "parentID INT DEFAULT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (reviewID) REFERENCES Reviews(reviewID) ON DELETE CASCADE, "
                + "FOREIGN KEY (parentID) REFERENCES ReviewFeedback(id) ON DELETE CASCADE"
                + ")";
        statement.execute(reviewFeedbackTable);

        String chatMessagesTable = "CREATE TABLE IF NOT EXISTS ChatMessages ("
                + "message_id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                + "question_id INTEGER NOT NULL, "
                + "reviewer_username TEXT NOT NULL, "
                + "student_username TEXT NOT NULL, "
                + "sender_role TEXT NOT NULL, "
                + "message TEXT NOT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(chatMessagesTable);

        // Create a table for reviewer weights
        String reviewerWeightsTable = "CREATE TABLE IF NOT EXISTS ReviewerWeights ("
                + "studentUsername VARCHAR(255), "
                + "reviewerUsername VARCHAR(255), "
                + "weight INT DEFAULT 0, "
                + "PRIMARY KEY (studentUsername, reviewerUsername))";
        statement.execute(reviewerWeightsTable);
        
        // Create a table for flagged content
        String flaggedContentTable = "CREATE TABLE IF NOT EXISTS FlaggedContent ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "contentType VARCHAR(20) NOT NULL, " // 'Question', 'Answer', or 'Feedback'
                + "contentID INT NOT NULL, "
                + "flaggedBy VARCHAR(255) NOT NULL, "
                + "flaggedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "description TEXT, "
                + "resolved BOOLEAN DEFAULT FALSE"
                + ")";
        statement.execute(flaggedContentTable);
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
    // Question Related Methods
    //================================================================================
    
    /**
     * Inserts a new question into the database.
     */
    public void insertQuestion(Question question) throws SQLException {
        ensureConnected();
        String query = "INSERT INTO Questions (questionID, bodyText, postedBy, dateCreated, "
                    + "resolvedStatus, acceptedAnsID, newMessagesCount) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, question.getQuestionID());
            pstmt.setString(2, question.getBodyText());
            pstmt.setString(3, question.getPostedBy());
            pstmt.setTimestamp(4, new Timestamp(question.getDateCreated().getTime()));
            pstmt.setBoolean(5, question.isResolved());
            pstmt.setInt(6, question.getAcceptedAnsID());
            pstmt.setInt(7, question.getNewMessagesCount());
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing question in the database.
     */
    public void updateQuestion(Question question) throws SQLException {
        ensureConnected();
        String query = "UPDATE Questions SET bodyText = ?, postedBy = ?, dateCreated = ?, "
                    + "resolvedStatus = ?, acceptedAnsID = ?, newMessagesCount = ? "
                    + "WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, question.getBodyText());
            pstmt.setString(2, question.getPostedBy());
            pstmt.setTimestamp(3, new Timestamp(question.getDateCreated().getTime()));
            pstmt.setBoolean(4, question.isResolved());
            pstmt.setInt(5, question.getAcceptedAnsID());
            pstmt.setInt(6, question.getNewMessagesCount());
            pstmt.setInt(7, question.getQuestionID());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Retrieves a specific question by ID.
     */
    public Question getQuestionById(int questionID) throws SQLException {
        ensureConnected();
        String query = "SELECT * FROM Questions WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Question q = new Question(
                        rs.getInt("questionID"),
                        rs.getString("bodyText"),
                        rs.getString("postedBy"),
                        rs.getTimestamp("dateCreated")
                    );
                    q.setResolved(rs.getBoolean("resolvedStatus"));
                    q.setAcceptedAnsID(rs.getInt("acceptedAnsID"));
                    q.setNewMessagesCount(rs.getInt("newMessagesCount"));
                    return q;
                }
            }
        }
        return null;
    }

    /**
     * Deletes a question from the database.
     */
    public void deleteQuestion(int questionID) throws SQLException {
        ensureConnected();
        // First delete all associated answers
        String deleteAnswers = "DELETE FROM Answers WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswers)) {
            pstmt.setInt(1, questionID);
            pstmt.executeUpdate();
        }
        
        // Then delete the question
        String deleteQuestion = "DELETE FROM Questions WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuestion)) {
            pstmt.setInt(1, questionID);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all questions from the database.
     */
    public List<Question> getAllQuestions() throws SQLException {
        ensureConnected();
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Question q = new Question(
                    rs.getInt("questionID"),
                    rs.getString("bodyText"),
                    rs.getString("postedBy"),
                    rs.getTimestamp("dateCreated")
                );
                q.setResolved(rs.getBoolean("resolvedStatus"));
                q.setAcceptedAnsID(rs.getInt("acceptedAnsID"));
                q.setNewMessagesCount(rs.getInt("newMessagesCount"));
                questions.add(q);
            }
        }
        return questions;
    }

    /**
     * Retrieves all questions posted by a specific student.
     * @param studentUsername The username of the student
     * @return A list of questions posted by the student
     */
    public List<Question> getQuestionsByStudent(String studentUsername) throws SQLException {
        ensureConnected();
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions WHERE postedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question(
                        rs.getInt("questionID"),
                        rs.getString("bodyText"),
                        rs.getString("postedBy"),
                        rs.getTimestamp("dateCreated")
                    );
                    q.setResolved(rs.getBoolean("resolvedStatus"));
                    q.setAcceptedAnsID(rs.getInt("acceptedAnsID"));
                    q.setNewMessagesCount(rs.getInt("newMessagesCount"));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    //================================================================================
    // Answer Related Methods
    //================================================================================

    /**
     * Inserts a new answer into the database.
     */
    public void insertAnswer(Answer answer) throws SQLException {
        ensureConnected();
        String query = "INSERT INTO Answers (answerID, questionID, bodyText, answeredBy, dateCreated) "
                    + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answer.getAnsID());
            pstmt.setInt(2, answer.getQuestionID());
            pstmt.setString(3, answer.getBodyText());
            pstmt.setString(4, answer.getAnsweredBy());
            pstmt.setTimestamp(5, new Timestamp(answer.getDateCreated().getTime()));
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing answer in the database.
     */
    public void updateAnswer(Answer answer) throws SQLException {
        ensureConnected();
        String query = "UPDATE Answers SET bodyText = ?, answeredBy = ?, dateCreated = ? "
                    + "WHERE answerID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, answer.getBodyText());
            pstmt.setString(2, answer.getAnsweredBy());
            pstmt.setTimestamp(3, new Timestamp(answer.getDateCreated().getTime()));
            pstmt.setInt(4, answer.getAnsID());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes an answer from the database.
     */
    public void deleteAnswer(int answerID) throws SQLException {
        ensureConnected();
        String query = "DELETE FROM Answers WHERE answerID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerID);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all answers for a specific question.
     */
    public List<Answer> getAnswersForQuestion(int questionID) throws SQLException {
        ensureConnected();
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM Answers WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer(
                        rs.getInt("answerID"),
                        rs.getInt("questionID"),
                        rs.getString("bodyText"),
                        rs.getString("answeredBy"),
                        rs.getTimestamp("dateCreated")
                    );
                    answers.add(a);
                }
            }
        }
        return answers;
    }

    /**
     * Retrieves all answers from the database.
     */
    public List<Answer> getAllAnswers() throws SQLException {
        ensureConnected();
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM Answers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Answer a = new Answer(
                    rs.getInt("answerID"),
                    rs.getInt("questionID"),
                    rs.getString("bodyText"),
                    rs.getString("answeredBy"),
                    rs.getTimestamp("dateCreated")
                );
                answers.add(a);
            }
        }
        return answers;
    }

    /**
     * Retrieves all answers posted by a specific student.
     * @param studentUsername The username of the student
     * @return A list of answers posted by the student
     */
    public List<Answer> getAnswersByStudent(String studentUsername) throws SQLException {
        ensureConnected();
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM Answers WHERE answeredBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer(
                        rs.getInt("answerID"),
                        rs.getInt("questionID"),
                        rs.getString("bodyText"),
                        rs.getString("answeredBy"),
                        rs.getTimestamp("dateCreated")
                    );
                    answers.add(a);
                }
            }
        }
        return answers;
    }

    /**
     * Marks an answer as accepted for a question and sets the question as resolved.
     * 
     * @param questionID The ID of the question
     * @param answerID The ID of the answer being accepted
     */
    public void acceptAnswer(int questionID, int answerID) throws SQLException {
        ensureConnected();
        
        // First retrieve the question to update its properties
        Question question = getQuestionById(questionID);
        if (question != null) {
            // Update the question with the accepted answer ID and mark it as resolved
            question.setAcceptedAnsID(answerID);
            question.setResolved(true);
            
            // Save the updated question back to the database
            updateQuestion(question);
        } else {
            throw new SQLException("Question not found with ID: " + questionID);
        }
    }
}