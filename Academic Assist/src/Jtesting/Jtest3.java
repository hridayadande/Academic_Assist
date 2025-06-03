package Jtesting;

/*******
 * <p> Title: Jtest3 Class. </p>
 * 
 * <p> Description: A Java demonstration for JUnit Automation tests for the Review System </p>
 * 
 * <p> Copyright: © 2025 </p>
 * 
 * @author Hridaya Amol Dande
 * 
 * @version 1.00
 * 
 */

/**
 * This test package contains unit tests for the Review System.
 * <p>
 * The following imports are used in this test class:
 * </p>
 * <ul>
 *   <li>JUnit 5 assertions - for test validations</li>
 *   <li>SQLException - for handling database exceptions</li>
 *   <li>Date and List - core Java utilities</li>
 *   <li>JUnit 5 annotations - for test lifecycle management</li>
 *   <li>DatabaseHelper2 - database connection and operations</li>
 *   <li>Question, Answer, User, Reviewer - core domain models</li>
 * </ul>
 */

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.PreparedStatement;
import java.util.UUID;

import application.Question;
import application.Answer;
import application.User;
import application.Reviewer;
import databasePart1.DatabaseHelper2;
import application.ReviewerRequest;
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper3;
/**
 * A test class for validating the operations of the Review System and its database interactions.
 * <p>
 * This class contains unit tests that verify the proper functionality of operations for managing 
 * reviews, reviewer requests, and user interactions. The tests cover the following key operations:
 * <ul>
 *   <li>Review creation and management</li>
 *   <li>Reviewer request processing</li>
 *   <li>Student-Reviewer interactions</li>
 *   <li>Trusted reviewer functionality</li>
 *   <li>Review weight management</li>
 * </ul>
 * <p>
 * Each test method follows a consistent pattern:
 * <ol>
 *   <li>Setting up test data</li>
 *   <li>Executing the operations being tested</li>
 *   <li>Verifying the expected outcomes</li>
 * </ol>
 * <p>
 * All tests utilize a database connection that is refreshed before each test execution
 * to ensure test isolation.
 * 
 * @see application.Question
 * @see application.Answer
 * @see application.User
 * @see application.Reviewer
 * @see application.ReviewerRequest
 * @see databasePart1.DatabaseHelper2
 * @see java.sql.SQLException
 */

public class Jtest3 {
    /** Database helper for database operations */
    private DatabaseHelper2 dbHelper2;
    
    /** Database helper for review and chat operations */
    private DatabaseHelper3 dbHelper3;
    
    /** Test reviewer for testing operations */
    private Reviewer reviewer;
    
    /** Test user (reviewer) for testing operations */
    private User testUser;
    
    /** Test student for testing operations */
    private User testStudent;
    
    /** Test question for testing operations */
    private Question testQuestion;
    
    /** Test answer for testing operations */
    private Answer testAnswer;

    /**
     * Sets up the test environment before each test.
     * <p>
     * This method initializes the database helper, establishes a database connection,
     * and creates test data including users, questions, and answers.
     * 
     * @throws SQLException if a database access error occurs
     * @see databasePart1.DatabaseHelper2#connectToDatabase()
     */
    @BeforeEach
    public void setUp() throws SQLException {
        dbHelper2 = new DatabaseHelper2();
        dbHelper2.connectToDatabase();
        dbHelper3 = new DatabaseHelper3();
        dbHelper3.connectToDatabase();
        reviewer = new Reviewer();
        
        // Create a test user (reviewer)
        testUser = new User("testReviewer", "password", "Test", "Reviewer", "test@example.com", "reviewer");
        
        // Create a test student
        testStudent = new User("testStudent", "password", "Test", "Student", "student@example.com", "student");
        
        // Create a test question
        int questionId = getNewQuestionId();
        testQuestion = new Question(questionId, "Test question for review testing", "testUser", new Date());
        dbHelper2.insertQuestion(testQuestion);
        
        // Create a test answer
        int answerId = getNewAnswerId();
        testAnswer = new Answer(answerId, questionId, "Test answer for review testing", "testUser", new Date());
        dbHelper2.insertAnswer(testAnswer);
    }

    /**
     * Cleans up resources after each test.
     * <p>
     * This method removes test data and closes the database connection to ensure proper 
     * resource management and test isolation.
     * 
     * @throws SQLException if a database access error occurs
     * @see databasePart1.DatabaseHelper2#closeConnection()
     */
    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up test data
        try {
            if (testAnswer != null) {
                dbHelper2.deleteAnswer(testAnswer.getAnsID());
            }
            if (testQuestion != null) {
                dbHelper2.deleteQuestion(testQuestion.getQuestionID());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper2.closeConnection();
            dbHelper3.closeConnection();
        }
    }

    @Nested
    @DisplayName("Review Creation Tests")
    class ReviewCreationTests {

        @Test
        @DisplayName("Create review for question")
        void testCreateQuestionReview() throws SQLException {
            // Create a review for the test question
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0, // answerID = 0 means it's a question review
                testUser.getUserName(),
                "This is a test review for the question",
                new Date()
            );
            
            // Verify the review was created
            List<String[]> reviews = dbHelper3.getReviewsForQuestionWithIDs(testQuestion.getQuestionID());
            assertFalse(reviews.isEmpty(), "Review should be created");
            
            // Check review details
            String[] review = reviews.get(0);
            assertEquals("This is a test review for the question", review[3], "Review text should match");
            assertEquals(testUser.getUserName(), review[4], "Reviewer name should match");
        }

        @Test
        @DisplayName("Create review for answer")
        void testCreateAnswerReview() throws SQLException {
            // Create a review for the test answer
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                testAnswer.getAnsID(),
                testUser.getUserName(),
                "This is a test review for the answer",
                new Date()
            );
            
            // Verify the review was created
            List<String[]> reviews = dbHelper3.getReviewsForAnswerWithIDs(testAnswer.getAnsID());
            assertFalse(reviews.isEmpty(), "Review should be created");
            
            // Check review details
            String[] review = reviews.get(0);
            assertEquals("This is a test review for the answer", review[3], "Review text should match");
            assertEquals(testUser.getUserName(), review[4], "Reviewer name should match");
        }
    }

    @Nested
    @DisplayName("Review Update Tests")
    class ReviewUpdateTests {

        @Test
        @DisplayName("Update existing review")
        void testUpdateReview() throws SQLException {
            // First create a review
            int reviewId = createTestReview();
            
            // Update the review
            String updatedText = "This is an updated review text";
            dbHelper3.updateReview(reviewId, updatedText);
            
            // Verify the review was updated
            List<String[]> allReviews = dbHelper3.getAllReviewsWithIDs();
            String[] updatedReview = allReviews.stream()
                .filter(r -> Integer.parseInt(r[0]) == reviewId)
                .findFirst()
                .orElse(null);
            
            assertNotNull(updatedReview, "Updated review should exist");
            assertEquals(updatedText, updatedReview[4], "Review text should be updated");
        }

        @Test
        @DisplayName("Verify review ownership")
        void testReviewOwnership() throws SQLException {
            // First create a review
            int reviewId = createTestReview();
            
            // Check if the review belongs to the test user
            boolean isOwner = dbHelper3.isReviewOwner(reviewId, testUser.getUserName());
            assertTrue(isOwner, "Test user should be the owner of the review");
            
            // Check if the review belongs to a different user
            boolean isNotOwner = dbHelper3.isReviewOwner(reviewId, "differentUser");
            assertFalse(isNotOwner, "Different user should not be the owner of the review");
        }
    }

    @Nested
    @DisplayName("Review Deletion Tests")
    class ReviewDeletionTests {

        @Test
        @DisplayName("Delete existing review")
        void testDeleteReview() throws SQLException {
            // First create a review
            int reviewId = createTestReview();
            
            // Delete the review
            dbHelper3.deleteReview(reviewId);
            
            // Verify the review was deleted
            List<String[]> allReviews = dbHelper3.getAllReviewsWithIDs();
            boolean reviewExists = allReviews.stream()
                .anyMatch(r -> Integer.parseInt(r[0]) == reviewId);
            
            assertFalse(reviewExists, "Review should be deleted");
        }
    }

    @Nested
    @DisplayName("Review Retrieval Tests")
    class ReviewRetrievalTests {

        @Test
        @DisplayName("Get all reviews")
        void testGetAllReviews() throws SQLException {
            // Create multiple reviews
            createTestReview();
            createTestReview();
            
            // Get all reviews
            List<String[]> allReviews = dbHelper3.getAllReviews();
            assertFalse(allReviews.isEmpty(), "Reviews should exist");
        }

        @Test
        @DisplayName("Get reviews for specific question")
        void testGetReviewsForQuestion() throws SQLException {
            // Create a review for the test question
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                testUser.getUserName(),
                "Test question review",
                new Date()
            );
            
            // Get reviews for the question
            List<String[]> questionReviews = dbHelper3.getReviewsForQuestionWithIDs(testQuestion.getQuestionID());
            assertFalse(questionReviews.isEmpty(), "Question reviews should exist");
            
            // Check review details
            String[] review = questionReviews.get(0);
            assertEquals(String.valueOf(testQuestion.getQuestionID()), review[1], "Question ID should match");
        }

        @Test
        @DisplayName("Get reviews for specific answer")
        void testGetReviewsForAnswer() throws SQLException {
            // Create a review for the test answer
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                testAnswer.getAnsID(),
                testUser.getUserName(),
                "Test answer review",
                new Date()
            );
            
            // Get reviews for the answer
            List<String[]> answerReviews = dbHelper3.getReviewsForAnswerWithIDs(testAnswer.getAnsID());
            assertFalse(answerReviews.isEmpty(), "Answer reviews should exist");
            
            // Check review details
            String[] review = answerReviews.get(0);
            assertEquals(String.valueOf(testAnswer.getAnsID()), review[1], "Answer ID should match");
        }
    }

    @Nested
    @DisplayName("Student Review Viewing Tests")
    class StudentReviewViewingTests {

        @Test
        @DisplayName("Student can view reviews for a question")
        void testStudentViewQuestionReviews() throws SQLException {
            // Create a review for the test question
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                testUser.getUserName(),
                "This is a review for students to see",
                new Date()
            );
            
            // Get reviews for the question as a student would
            List<String[]> questionReviews = dbHelper3.getReviewsForQuestionWithIDs(testQuestion.getQuestionID());
            assertFalse(questionReviews.isEmpty(), "Student should be able to see question reviews");
            
            // Check review details
            String[] review = questionReviews.get(0);
            assertEquals("This is a review for students to see", review[3], "Review text should be visible to student");
            assertEquals(testUser.getUserName(), review[4], "Reviewer name should be visible to student");
        }

        @Test
        @DisplayName("Student can view reviews for an answer")
        void testStudentViewAnswerReviews() throws SQLException {
            // Create a review for the test answer
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                testAnswer.getAnsID(),
                testUser.getUserName(),
                "This is an answer review for students to see",
                new Date()
            );
            
            // Get reviews for the answer as a student would
            List<String[]> answerReviews = dbHelper3.getReviewsForAnswerWithIDs(testAnswer.getAnsID());
            assertFalse(answerReviews.isEmpty(), "Student should be able to see answer reviews");
            
            // Check review details
            String[] review = answerReviews.get(0);
            assertEquals("This is an answer review for students to see", review[3], "Review text should be visible to student");
            assertEquals(testUser.getUserName(), review[4], "Reviewer name should be visible to student");
        }

        @Test
        @DisplayName("Student can view all reviews")
        void testStudentViewAllReviews() throws SQLException {
            // Create multiple reviews
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                testUser.getUserName(),
                "Question review 1",
                new Date()
            );
            
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                testAnswer.getAnsID(),
                testUser.getUserName(),
                "Answer review 1",
                new Date()
            );
            
            // Get all reviews as a student would
            List<String[]> allReviews = dbHelper3.getAllReviews();
            assertFalse(allReviews.isEmpty(), "Student should be able to see all reviews");
            
            // Verify both question and answer reviews are included
            boolean hasQuestionReview = allReviews.stream()
                .anyMatch(r -> r[0].equals("Question") && r[3].equals("Question review 1"));
            boolean hasAnswerReview = allReviews.stream()
                .anyMatch(r -> r[0].equals("Answer") && r[3].equals("Answer review 1"));
            
            assertTrue(hasQuestionReview, "Student should see question review");
            assertTrue(hasAnswerReview, "Student should see answer review");
        }
    }

    @Nested
    @DisplayName("Student-Reviewer Private Messaging Tests")
    class StudentReviewerMessagingTests {

        @Test
        @DisplayName("Student can send private message to reviewer")
        void testStudentSendPrivateMessage() throws SQLException {
            // Send a private message from student to reviewer
            String messageText = "This is a private message from student to reviewer";
            dbHelper3.insertGeneralChatMessage("Student", testStudent.getUserName(), testUser.getUserName(), messageText);
            
            // Verify the message was sent by retrieving the chat history
            List<String> chatHistory = dbHelper3.getChatBetweenUsers(testStudent.getUserName(), testUser.getUserName());
            assertFalse(chatHistory.isEmpty(), "Chat history should not be empty");
            
            // Check if the message is in the chat history
            boolean messageFound = chatHistory.stream()
                .anyMatch(msg -> msg.contains(messageText));
            assertTrue(messageFound, "Message should be in chat history");
        }
        
        @Test
        @DisplayName("Student can view chat history with reviewer")
        void testStudentViewChatHistory() throws SQLException {
            // Send multiple messages
            String message1 = "First message from student";
            String message2 = "Second message from student";
            
            dbHelper3.insertGeneralChatMessage("Student", testStudent.getUserName(), testUser.getUserName(), message1);
            dbHelper3.insertGeneralChatMessage("Student", testStudent.getUserName(), testUser.getUserName(), message2);
            
            // Get chat history
            List<String> chatHistory = dbHelper3.getChatBetweenUsers(testStudent.getUserName(), testUser.getUserName());
            
            // Verify both messages are in the chat history
            assertTrue(chatHistory.size() >= 2, "Chat history should contain at least 2 messages");
            
            boolean message1Found = chatHistory.stream()
                .anyMatch(msg -> msg.contains(message1));
            boolean message2Found = chatHistory.stream()
                .anyMatch(msg -> msg.contains(message2));
                
            assertTrue(message1Found, "First message should be in chat history");
            assertTrue(message2Found, "Second message should be in chat history");
        }
        
        @Test
        @DisplayName("Reviewer can send private message to student")
        void testReviewerSendPrivateMessage() throws SQLException {
            // Send a private message from reviewer to student
            String messageText = "This is a private message from reviewer to student";
            dbHelper3.insertGeneralChatMessage("Reviewer", testUser.getUserName(), testStudent.getUserName(), messageText);
            
            // Verify the message was sent by retrieving the chat history
            List<String> chatHistory = dbHelper3.getChatBetweenUsers(testStudent.getUserName(), testUser.getUserName());
            assertFalse(chatHistory.isEmpty(), "Chat history should not be empty");
            
            // Check if the message is in the chat history
            boolean messageFound = chatHistory.stream()
                .anyMatch(msg -> msg.contains(messageText));
            assertTrue(messageFound, "Message should be in chat history");
        }
        
        @Test
        @DisplayName("Chat history contains messages from both student and reviewer")
        void testChatHistoryContainsBothSides() throws SQLException {
            // Send messages from both sides
            String studentMessage = "Message from student";
            String reviewerMessage = "Message from reviewer";
            
            dbHelper3.insertGeneralChatMessage("Student", testStudent.getUserName(), testUser.getUserName(), studentMessage);
            dbHelper3.insertGeneralChatMessage("Reviewer", testUser.getUserName(), testStudent.getUserName(), reviewerMessage);
            
            // Get chat history
            List<String> chatHistory = dbHelper3.getChatBetweenUsers(testStudent.getUserName(), testUser.getUserName());
            
            // Verify both messages are in the chat history
            assertTrue(chatHistory.size() >= 2, "Chat history should contain at least 2 messages");
            
            boolean studentMessageFound = chatHistory.stream()
                .anyMatch(msg -> msg.contains(studentMessage));
            boolean reviewerMessageFound = chatHistory.stream()
                .anyMatch(msg -> msg.contains(reviewerMessage));
                
            assertTrue(studentMessageFound, "Student message should be in chat history");
            assertTrue(reviewerMessageFound, "Reviewer message should be in chat history");
        }
    }

    @Nested
    @DisplayName("Trusted Reviewer Tests")
    class TrustedReviewerTests {

        @Test
        @DisplayName("Student can add trusted reviewer")
        void testAddTrustedReviewer() throws SQLException {
            // Set a reviewer weight (this marks the reviewer as trusted)
            int weight = 5;
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), weight);
            
            // Verify the reviewer weight was set correctly
            int retrievedWeight = dbHelper3.getReviewerWeight(testStudent.getUserName(), testUser.getUserName());
            assertEquals(weight, retrievedWeight, "Reviewer weight should be set correctly");
        }
        
        @Test
        @DisplayName("Student can update trusted reviewer weight")
        void testUpdateTrustedReviewerWeight() throws SQLException {
            // First set an initial weight
            int initialWeight = 3;
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), initialWeight);
            
            // Update the weight
            int updatedWeight = 7;
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), updatedWeight);
            
            // Verify the weight was updated
            int retrievedWeight = dbHelper3.getReviewerWeight(testStudent.getUserName(), testUser.getUserName());
            assertEquals(updatedWeight, retrievedWeight, "Reviewer weight should be updated correctly");
        }
        
        @Test
        @DisplayName("Student can get all trusted reviewers")
        void testGetAllTrustedReviewers() throws SQLException {
            // Add multiple trusted reviewers
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), 5);
            
            // Create another reviewer
            User anotherReviewer = new User("anotherReviewer", "password", "Another", "Reviewer", "another@example.com", "reviewer");
            dbHelper3.setReviewerWeight(testStudent.getUserName(), anotherReviewer.getUserName(), 3);
            
            // Get all trusted reviewers
            List<String[]> trustedReviewers = dbHelper3.getReviewerWeights(testStudent.getUserName());
            
            // Verify the trusted reviewers are retrieved
            assertFalse(trustedReviewers.isEmpty(), "Trusted reviewers list should not be empty");
            assertTrue(trustedReviewers.size() >= 2, "Should have at least 2 trusted reviewers");
            
            // Check if both reviewers are in the list
            boolean firstReviewerFound = trustedReviewers.stream()
                .anyMatch(r -> r[0].equals(testUser.getUserName()) && Integer.parseInt(r[1]) == 5);
            boolean secondReviewerFound = trustedReviewers.stream()
                .anyMatch(r -> r[0].equals(anotherReviewer.getUserName()) && Integer.parseInt(r[1]) == 3);
                
            assertTrue(firstReviewerFound, "First reviewer should be in trusted reviewers list");
            assertTrue(secondReviewerFound, "Second reviewer should be in trusted reviewers list");
        }
        @Test
        @DisplayName("Reviews can be filtered by trusted reviewers")
        void testFilterReviewsByTrustedReviewers() throws SQLException {
            // Clean up any existing reviewer weights
            List<String[]> existingWeights = dbHelper3.getReviewerWeights(testStudent.getUserName());
            for (String[] weight : existingWeights) {
                dbHelper3.setReviewerWeight(testStudent.getUserName(), weight[0], 0);
            }
            
            // Create a trusted reviewer
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), 5);
            
            // Create another reviewer (not trusted)
            User anotherReviewer = new User("anotherReviewer", "password", "Another", "Reviewer", "another@example.com", "reviewer");
            // Explicitly set weight to 0 for non-trusted reviewer
            dbHelper3.setReviewerWeight(testStudent.getUserName(), anotherReviewer.getUserName(), 0);
            
            // Create reviews from both reviewers
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0, // answerID = 0 means it's a question review
                testUser.getUserName(),
                "Review from trusted reviewer",
                new Date()
            );
            
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                anotherReviewer.getUserName(),
                "Review from non-trusted reviewer",
                new Date()
            );
            
            // Get all reviews for the question
            List<String[]> allReviews = dbHelper3.getReviewsForQuestionWithIDs(testQuestion.getQuestionID());
            assertTrue(allReviews.size() >= 2, "Should have at least 2 reviews");
            
            // Filter reviews by trusted reviewers
            List<String[]> trustedReviews = allReviews.stream()
                .filter(review -> {
                    try {
                        int weight = dbHelper3.getReviewerWeight(testStudent.getUserName(), review[4]); // reviewer name is at index 4
                        return weight > 0; // Any positive weight means trusted
                    } catch (SQLException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
            
            // Verify only trusted reviewer's reviews are included
            assertEquals(1, trustedReviews.size(), "Should have exactly 1 trusted review");
            assertEquals("Review from trusted reviewer", trustedReviews.get(0)[3], "Trusted review text should match");
            assertEquals(testUser.getUserName(), trustedReviews.get(0)[4], "Trusted reviewer name should match");
        }
    }

    @Nested
    @DisplayName("Review Weight Tests")
    class ReviewWeightTests {

        @Test
        @DisplayName("Test setting initial weight for a reviewer")
        void testSetInitialWeight() throws SQLException {
            // Set initial weight
            int initialWeight = 5;
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), initialWeight);
            
            // Verify the weight was set correctly
            int retrievedWeight = dbHelper3.getReviewerWeight(testStudent.getUserName(), testUser.getUserName());
            assertEquals(initialWeight, retrievedWeight, "Initial weight should be set correctly");
        }

        @Test
        @DisplayName("Test updating existing weight for a reviewer")
        void testUpdateWeight() throws SQLException {
            // Set initial weight
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), 3);
            
            // Update the weight
            int newWeight = 7;
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), newWeight);
            
            // Verify the weight was updated correctly
            int retrievedWeight = dbHelper3.getReviewerWeight(testStudent.getUserName(), testUser.getUserName());
            assertEquals(newWeight, retrievedWeight, "Weight should be updated correctly");
        }

        @Test
        @DisplayName("Test getting all reviewer weights for a student")
        void testGetAllReviewerWeights() throws SQLException {
            // Clean up any existing reviewer weights for this student
            List<String[]> existingWeights = dbHelper3.getReviewerWeights(testStudent.getUserName());
            for (String[] weight : existingWeights) {
                // Delete the weight by setting it to 0
                dbHelper3.setReviewerWeight(testStudent.getUserName(), weight[0], 0);
            }
            
            // Verify all weights are cleaned up
            List<String[]> cleanedWeights = dbHelper3.getReviewerWeights(testStudent.getUserName());
            assertTrue(cleanedWeights.isEmpty() || 
                      cleanedWeights.stream().allMatch(w -> Integer.parseInt(w[1]) == 0), 
                      "All weights should be cleaned up");
            
            // Set weights for multiple reviewers
            User reviewer2 = new User("reviewer2", "password", "Test", "Reviewer2", "reviewer2@test.com", "reviewer");
            
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), 5);
            dbHelper3.setReviewerWeight(testStudent.getUserName(), reviewer2.getUserName(), 3);
            
            // Get all weights
            List<String[]> weights = dbHelper3.getReviewerWeights(testStudent.getUserName());
            
            // Filter out any weights that are 0 (not trusted reviewers)
            List<String[]> trustedWeights = weights.stream()
                .filter(w -> Integer.parseInt(w[1]) > 0)
                .collect(Collectors.toList());
            
            // Verify the results
            assertFalse(trustedWeights.isEmpty(), "Should have weights for reviewers");
            assertEquals(2, trustedWeights.size(), "Should have weights for both reviewers");
            
            // Check if both reviewers are in the list
            boolean hasFirstReviewer = trustedWeights.stream()
                .anyMatch(w -> w[0].equals(testUser.getUserName()) && Integer.parseInt(w[1]) == 5);
            boolean hasSecondReviewer = trustedWeights.stream()
                .anyMatch(w -> w[0].equals(reviewer2.getUserName()) && Integer.parseInt(w[1]) == 3);
                
            assertTrue(hasFirstReviewer, "Should have weight for first reviewer");
            assertTrue(hasSecondReviewer, "Should have weight for second reviewer");
        }

        @Test
        @DisplayName("Test getting weight for non-existent reviewer")
        void testGetWeightForNonExistentReviewer() throws SQLException {
            // Try to get weight for a reviewer that hasn't been set
            int weight = dbHelper3.getReviewerWeight(testStudent.getUserName(), "nonExistentReviewer");
            assertEquals(0, weight, "Should return 0 for non-existent reviewer");
        }

        @Test
        @DisplayName("Test removing reviewer from trusted reviewers by setting weight to zero")
        void testRemoveReviewerFromTrustedReviewers() throws SQLException {
            // First set a positive weight to make the reviewer trusted
            int initialWeight = 5;
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), initialWeight);
            
            // Verify the reviewer is in the trusted reviewers list
            List<String[]> trustedReviewers = dbHelper3.getReviewerWeights(testStudent.getUserName());
            List<String[]> filteredReviewers = trustedReviewers.stream()
                .filter(w -> Integer.parseInt(w[1]) > 0)
                .collect(Collectors.toList());
            
            assertTrue(filteredReviewers.stream()
                .anyMatch(w -> w[0].equals(testUser.getUserName()) && Integer.parseInt(w[1]) == initialWeight),
                "Reviewer should be in trusted reviewers list");
            
            // Set weight to zero to remove from trusted reviewers
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testUser.getUserName(), 0);
            
            // Verify the reviewer is no longer in the trusted reviewers list
            List<String[]> updatedTrustedReviewers = dbHelper3.getReviewerWeights(testStudent.getUserName());
            List<String[]> updatedFilteredReviewers = updatedTrustedReviewers.stream()
                .filter(w -> Integer.parseInt(w[1]) > 0)
                .collect(Collectors.toList());
            
            assertFalse(updatedFilteredReviewers.stream()
                .anyMatch(w -> w[0].equals(testUser.getUserName()) && Integer.parseInt(w[1]) > 0),
                "Reviewer should not be in trusted reviewers list after setting weight to zero");
            
            // Verify the weight is still stored as zero
            int retrievedWeight = dbHelper3.getReviewerWeight(testStudent.getUserName(), testUser.getUserName());
            assertEquals(0, retrievedWeight, "Weight should be zero after removal");
        }

    }

    @Nested
    @DisplayName("Instructor Reviewer Request Tests")
    class InstructorReviewerRequestTests {
        private DatabaseHelper dbHelper;
        private User testInstructor;
        private ReviewerRequest testRequest;
        private Question studentQuestion;
        private Answer studentAnswer;
        
        @BeforeEach
        public void setUpInstructorTests() throws SQLException {
            dbHelper = new DatabaseHelper();
            dbHelper.connectToDatabase();
            
            // Create a test instructor
            testInstructor = new User("testInstructor", "password", "Test", "Instructor", "instructor@example.com", "instructor");
            
            // Create a test student (if not already created)
            if (testStudent == null) {
                testStudent = new User("testStudent", "password", "Test", "Student", "student@example.com", "student");
            }
            
            // Create a test reviewer request with a unique ID
            String requestId = UUID.randomUUID().toString();
            testRequest = new ReviewerRequest(
                testStudent.getUserName(),
                testInstructor.getUserName(),
                "I would like to become a reviewer",
                java.time.LocalDateTime.now()
            );
            testRequest.setRequestID(requestId);
            
            // Create a test question by the student
            int questionId = getNewQuestionId();
            studentQuestion = new Question(questionId, "Student question for testing", testStudent.getUserName(), new Date());
            dbHelper2.insertQuestion(studentQuestion);
            
            // Create a test answer by the student
            int answerId = getNewAnswerId();
            studentAnswer = new Answer(answerId, questionId, "Student answer for testing", testStudent.getUserName(), new Date());
            dbHelper2.insertAnswer(studentAnswer);
            
            // Insert the reviewer request
            dbHelper.insertReviewerRequest(testRequest);
        }
        
        @AfterEach
        public void tearDownInstructorTests() throws SQLException {
            try {
                // Clean up test data
                if (studentAnswer != null) {
                    dbHelper2.deleteAnswer(studentAnswer.getAnsID());
                }
                if (studentQuestion != null) {
                    dbHelper2.deleteQuestion(studentQuestion.getQuestionID());
                }
                
                // Reset student role if it was changed
                dbHelper.updateUserRole(testStudent.getUserName(), "student");
                
                // Delete the reviewer request - we need to implement this method
                // For now, we'll just update the status to a non-pending state
                try {
                    dbHelper.updateReviewerRequestStatus(testRequest.getRequestID(), "DELETED");
                } catch (SQLException e) {
                    // Ignore if the request was already deleted
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbHelper.closeConnection();
            }
        }
        
        @Test
        @DisplayName("Instructor can view student questions")
        void testInstructorViewStudentQuestions() throws SQLException {
            // Get questions posted by the student
            List<Question> studentQuestions = dbHelper2.getQuestionsByStudent(testStudent.getUserName());
            
            // Verify the student's question is in the list
            assertFalse(studentQuestions.isEmpty(), "Student should have questions");
            assertEquals(studentQuestion.getQuestionID(), studentQuestions.get(0).getQuestionID(), 
                "Question ID should match");
            assertEquals(studentQuestion.getBodyText(), studentQuestions.get(0).getBodyText(), 
                "Question text should match");
        }
        
        @Test
        @DisplayName("Instructor can view student answers")
        void testInstructorViewStudentAnswers() throws SQLException {
            // Get answers posted by the student
            List<Answer> studentAnswers = dbHelper2.getAnswersByStudent(testStudent.getUserName());
            
            // Verify the student's answer is in the list
            assertFalse(studentAnswers.isEmpty(), "Student should have answers");
            assertEquals(studentAnswer.getAnsID(), studentAnswers.get(0).getAnsID(), 
                "Answer ID should match");
            assertEquals(studentAnswer.getBodyText(), studentAnswers.get(0).getBodyText(), 
                "Answer text should match");
        }
        
        @Test
        @DisplayName("Instructor can view reviewer requests")
        void testInstructorViewReviewerRequests() throws SQLException {
            // Get reviewer requests for the instructor
            List<ReviewerRequest> requests = dbHelper.getReviewerRequestsForInstructor(testInstructor.getUserName());
            
            // Verify the test request is in the list
            assertFalse(requests.isEmpty(), "Instructor should have reviewer requests");
            
            // Find our test request in the list
            boolean foundRequest = false;
            for (ReviewerRequest request : requests) {
                if (request.getRequestID().equals(testRequest.getRequestID())) {
                    foundRequest = true;
                    assertEquals(testStudent.getUserName(), request.getStudentName(), 
                        "Student name should match");
                    assertEquals(testInstructor.getUserName(), request.getInstructorUsername(), 
                        "Instructor username should match");
                    break;
                }
            }
            
            assertTrue(foundRequest, "Test request should be found in the list");
        }
        
        @Test
        @DisplayName("Instructor can accept reviewer request")
        void testInstructorAcceptReviewerRequest() throws SQLException {
            String currentRole = "";
        	// First ensure the student exists in the database
            try {
                currentRole = dbHelper.getUserRole(testStudent.getUserName());
                if (currentRole == null) {
                    // User doesn't exist, register it
                    dbHelper.register(testStudent);
                    currentRole = "student"; // Default role
                }
            } catch (SQLException e) {
                // If there's an error, register the user
                dbHelper.register(testStudent);
                currentRole = "student"; // Default role
            }
            
            // Accept the reviewer request
            dbHelper.updateReviewerRequestStatus(testRequest.getRequestID(), "ACCEPTED");
            
            // Update the student's role to include Reviewer
            String newRole = currentRole + ",Reviewer";
            dbHelper.updateUserRole(testStudent.getUserName(), newRole);
            
            // Verify the student's role was updated
            String updatedRole = dbHelper.getUserRole(testStudent.getUserName());
            assertNotNull(updatedRole, "User role should not be null");
            assertTrue(updatedRole.contains("Reviewer"), "Student should now have Reviewer role");
        }
        
        @Test
        @DisplayName("Instructor can view student activity after accepting request")
        void testInstructorViewStudentActivityAfterAccepting() throws SQLException {
            // First accept the request and update the role
            dbHelper.updateReviewerRequestStatus(testRequest.getRequestID(), "ACCEPTED");
            String currentRole = dbHelper.getUserRole(testStudent.getUserName());
            String newRole = currentRole + ",Reviewer";
            dbHelper.updateUserRole(testStudent.getUserName(), newRole);
            
            // Verify the student's questions are still accessible
            List<Question> studentQuestions = dbHelper2.getQuestionsByStudent(testStudent.getUserName());
            assertFalse(studentQuestions.isEmpty(), "Student should still have questions after role change");
            
            // Verify the student's answers are still accessible
            List<Answer> studentAnswers = dbHelper2.getAnswersByStudent(testStudent.getUserName());
            assertFalse(studentAnswers.isEmpty(), "Student should still have answers after role change");
        }
        
        @Test
        @DisplayName("Instructor can see multiple student questions and answers")
        void testInstructorViewMultipleStudentActivities() throws SQLException {
            // Create additional questions and answers by the student
            int questionId2 = getNewQuestionId();
            Question studentQuestion2 = new Question(questionId2, "Second student question", testStudent.getUserName(), new Date());
            dbHelper2.insertQuestion(studentQuestion2);
            
            int answerId2 = getNewAnswerId();
            Answer studentAnswer2 = new Answer(answerId2, questionId2, "Second student answer", testStudent.getUserName(), new Date());
            dbHelper2.insertAnswer(studentAnswer2);
            
            // Get all questions and answers
            List<Question> studentQuestions = dbHelper2.getQuestionsByStudent(testStudent.getUserName());
            List<Answer> studentAnswers = dbHelper2.getAnswersByStudent(testStudent.getUserName());
            
            // Verify multiple questions and answers are returned
            assertTrue(studentQuestions.size() >= 2, "Student should have at least 2 questions");
            assertTrue(studentAnswers.size() >= 2, "Student should have at least 2 answers");
            
            // Clean up the additional test data
            dbHelper2.deleteAnswer(answerId2);
            dbHelper2.deleteQuestion(questionId2);
        }
    }

    /**
     * Helper method to create a test review and return its ID
     */
    private int createTestReview() throws SQLException {
        // Create a review for the test question
        dbHelper3.insertReview(
            testQuestion.getQuestionID(),
            0,
            testUser.getUserName(),
            "Test review " + System.currentTimeMillis(),
            new Date()
        );
        
        // Get the ID of the created review
        List<String[]> reviews = dbHelper3.getReviewsForQuestionWithIDs(testQuestion.getQuestionID());
        return Integer.parseInt(reviews.get(0)[0]);
    }

    /**
     * Helper method to get a new question ID
     */
    private int getNewQuestionId() throws SQLException {
        List<Question> allQuestions = dbHelper2.getAllQuestions();
        return allQuestions.stream()
            .mapToInt(Question::getQuestionID)
            .max()
            .orElse(0) + 1;
    }

    /**
     * Helper method to get a new answer ID
     */
    private int getNewAnswerId() throws SQLException {
        List<Answer> allAnswers = dbHelper2.getAllAnswers();
        return allAnswers.stream()
            .mapToInt(Answer::getAnsID)
            .max()
            .orElse(0) + 1;
    }
}
