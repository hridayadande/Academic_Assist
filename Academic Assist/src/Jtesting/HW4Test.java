package Jtesting;

/*******
 * <p> Title: HW4Test Class. </p>
 * 
 * <p> Description: JUnit tests for staff user stories in the Questions and Answers System </p>
 * 
 * <p> Copyright: © 2025 </p>
 * 
 * @author Hridaya Amol Dande
 * 
 * @version 1.00
 * 
 */

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import application.User;
import application.Question;
import application.Answer;
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper3;
import databasePart1.DatabaseHelper2;

/**
 * A test class for validating the implementation of staff user stories:
 * <ol>
 *   <li>Staff viewing all interactions</li>
 *   <li>Staff sending private messages to students</li>
 *   <li>Staff responding to student messages</li>
 *   <li>Staff flagging inappropriate content</li>
 *   <li>Staff requesting student restrictions</li>
 *   <li>Instructors restricting/unrestricting students</li>
 * </ol>
 */
public class HW4Test {
    /** Database helper for user operations */
    private DatabaseHelper dbHelper;
    
    /** Database helper for question and answer operations */
    private DatabaseHelper2 dbHelper2;
    
    /** Database helper for advanced operations */
    private DatabaseHelper3 dbHelper3;
    
    /** Test user with staff role */
    private User testStaff;
    
    /** Test user with student role */
    private User testStudent;
    
    /** Test user with instructor role */
    private User testInstructor;
    
    /** Test question for interactions */
    private Question testQuestion;

    /**
     * Sets up the test environment before each test.
     * 
     * @throws SQLException if a database access error occurs
     */
    @BeforeEach
    public void setUp() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper2 = new DatabaseHelper2();
        dbHelper3 = new DatabaseHelper3();
        dbHelper.connectToDatabase();
        dbHelper2.connectToDatabase();
        dbHelper3.connectToDatabase();
        
        // Create test users if they don't exist
        String staffUsername = "teststaff" + UUID.randomUUID().toString().substring(0, 8);
        testStaff = new User(staffUsername, "Test", "Staff", "test@staff.com", "password", "Staff");
        
        String studentUsername = "teststudent" + UUID.randomUUID().toString().substring(0, 8);
        testStudent = new User(studentUsername, "Test", "Student", "test@student.com", "password", "Student");
        
        String instructorUsername = "testinstructor" + UUID.randomUUID().toString().substring(0, 8);
        testInstructor = new User(instructorUsername, "Test", "Instructor", "test@instructor.com", "password", "Instructor");
        
        if (!dbHelper.doesUserExist(testStaff.getUserName())) {
            dbHelper.register(testStaff);
        }
        
        if (!dbHelper.doesUserExist(testStudent.getUserName())) {
            dbHelper.register(testStudent);
        }
        
        if (!dbHelper.doesUserExist(testInstructor.getUserName())) {
            dbHelper.register(testInstructor);
        }
        
        // Create a test question
        int newId = getNewQuestionId();
        testQuestion = new Question(newId, "Test question for staff features", testStudent.getUserName(), new Date());
        dbHelper2.insertQuestion(testQuestion);
    }

    /**
     * Cleans up resources after each test.
     */
    @AfterEach
    public void tearDown() {
        try {
            // Clean up test data if needed
            if (testQuestion != null) {
                dbHelper2.deleteQuestion(testQuestion.getQuestionID());
            }
        } catch (SQLException e) {
            // Ignore errors during cleanup
        }
        
        // Close all database connections
        if (dbHelper != null) {
            try {
                dbHelper.closeConnection();
            } catch (Exception e) {
                // Ignore errors during cleanup
            }
        }
        
        if (dbHelper2 != null) {
            try {
                dbHelper2.closeConnection();
            } catch (Exception e) {
                // Ignore errors during cleanup
            }
        }
        
        if (dbHelper3 != null) {
            try {
                dbHelper3.closeConnection();
            } catch (Exception e) {
                // Ignore errors during cleanup
            }
        }
    }

    /**
     * Utility method to get a new unique question ID
     */
    private int getNewQuestionId() throws SQLException {
        List<Question> allQuestions = dbHelper2.getAllQuestions();
        return allQuestions.stream()
            .mapToInt(Question::getQuestionID)
            .max()
            .orElse(0) + 1;
    }

    @Nested
    @DisplayName("User Story 1: Staff Viewing All Interactions")
    class StaffViewingInteractionsTests {

        @Test
        @DisplayName("Staff can view all questions")
        void testStaffCanViewAllQuestions() throws SQLException {
            // Verify that a staff member can view all questions
            List<Question> questions = dbHelper2.getAllQuestions();
            assertNotNull(questions);
            assertFalse(questions.isEmpty());
            assertTrue(questions.stream().anyMatch(q -> q.getQuestionID() == testQuestion.getQuestionID()));
        }
        
        @Test
        @DisplayName("Staff can view answers for a question")
        void testStaffCanViewAnswersForQuestion() throws SQLException {
            // Create a test answer
            Answer testAnswer = new Answer(
                getNewAnswerId(),
                testQuestion.getQuestionID(),
                "This is a test answer from a student",
                testStudent.getUserName(),
                new Date()
            );
            dbHelper2.insertAnswer(testAnswer);
            
            // Verify that a staff member can view all answers for a question
            List<Answer> answers = dbHelper2.getAnswersForQuestion(testQuestion.getQuestionID());
            assertNotNull(answers);
            assertFalse(answers.isEmpty());
            assertTrue(answers.stream().anyMatch(a -> a.getAnsID() == testAnswer.getAnsID()));
        }
        
        @Test
        @DisplayName("Staff can view feedback")
        void testStaffCanViewFeedback() throws SQLException {
            // Create test feedback
            String feedbackText = "This is test feedback";
            dbHelper3.insertFeedback(
                testQuestion.getQuestionID(),
                testStudent.getUserName(),
                testStaff.getUserName(),
                feedbackText
            );
            
            // Get feedback for the student
            List<String[]> feedback = dbHelper3.getFeedbackForUser(testStudent.getUserName());
            assertNotNull(feedback);
            assertFalse(feedback.isEmpty());
            
            // Verify the feedback is visible
            boolean feedbackFound = feedback.stream()
                .anyMatch(f -> f[4].equals(feedbackText) && f[5].equals(testStaff.getUserName()));
            assertTrue(feedbackFound);
        }
        
        /**
         * Utility method to get a new unique answer ID
         */
        private int getNewAnswerId() throws SQLException {
            List<Answer> allAnswers = dbHelper2.getAllAnswers();
            return allAnswers.stream()
                .mapToInt(Answer::getAnsID)
                .max()
                .orElse(0) + 1;
        }
    }
    
    @Nested
    @DisplayName("User Story 2 & 3: Staff Messaging Features")
    class StaffMessagingTests {

        @Test
        @DisplayName("Staff can send messages to students")
        void testStaffCanSendMessagesToStudents() throws SQLException {
            // Staff sends a message to a student about a question
            String messageText = "This is a test message from staff to student";
            dbHelper3.insertChatMessage("Staff", testStaff.getUserName(), testQuestion.getQuestionID(), messageText);
            
            // Get the messages for the question
            List<String> messages = dbHelper3.getChatMessagesForQuestion(testStudent.getUserName(), testQuestion.getQuestionID());
            
            // Verify the message is visible
            assertNotNull(messages);
            assertFalse(messages.isEmpty());
            assertTrue(messages.stream().anyMatch(m -> m.contains(messageText)));
        }
        
        @Test
        @DisplayName("Staff can receive and respond to student messages")
        void testStaffCanReceiveAndRespondToStudentMessages() throws SQLException {
            // Student sends a message
            String studentMessage = "This is a test message from student to staff";
            dbHelper3.insertChatMessage("Student", testStudent.getUserName(), testQuestion.getQuestionID(), studentMessage);
            
            // Staff responds to the message
            String staffResponse = "This is a test response from staff to student";
            dbHelper3.insertChatMessage("Staff", testStaff.getUserName(), testQuestion.getQuestionID(), staffResponse);
            
            // Get the messages for the question
            List<String> messages = dbHelper3.getChatMessagesForQuestion(testStudent.getUserName(), testQuestion.getQuestionID());
            
            // Verify both messages are visible
            assertNotNull(messages);
            assertTrue(messages.size() >= 2);
            assertTrue(messages.stream().anyMatch(m -> m.contains(studentMessage)));
            assertTrue(messages.stream().anyMatch(m -> m.contains(staffResponse)));
        }
    }
    
    @Nested
    @DisplayName("User Story 4: Staff Flagging Inappropriate Content")
    class StaffFlaggingTests {

        @Test
        @DisplayName("Staff can flag questions")
        void testStaffCanFlagQuestions() throws SQLException {
            // Staff flags a question
            String flagReason = "This is a test flag reason";
            dbHelper3.flagContent("Question", testQuestion.getQuestionID(), testStaff.getUserName(), flagReason);
            
            // Get all flagged content
            List<String[]> flaggedContent = dbHelper3.getAllFlaggedContent();
            
            // Verify the question was flagged
            assertNotNull(flaggedContent);
            assertFalse(flaggedContent.isEmpty());
            
            boolean flagFound = flaggedContent.stream()
                .anyMatch(f -> f[1].equals("Question") && 
                          f[2].equals(String.valueOf(testQuestion.getQuestionID())) && 
                          f[3].equals(testStaff.getUserName()) && 
                          f[5].equals(flagReason));
            assertTrue(flagFound);
        }
        
        @Test
        @DisplayName("Staff can flag answers")
        void testStaffCanFlagAnswers() throws SQLException {
            // Create and insert a test answer
            Answer testAnswer = new Answer(
                getNewAnswerId(),
                testQuestion.getQuestionID(),
                "This is a test answer for flagging",
                testStudent.getUserName(),
                new Date()
            );
            dbHelper2.insertAnswer(testAnswer);
            
            // Staff flags the answer
            String flagReason = "This is a test flag reason for an answer";
            dbHelper3.flagContent("Answer", testAnswer.getAnsID(), testStaff.getUserName(), flagReason);
            
            // Get all flagged content
            List<String[]> flaggedContent = dbHelper3.getAllFlaggedContent();
            
            // Verify the answer was flagged
            assertNotNull(flaggedContent);
            assertFalse(flaggedContent.isEmpty());
            
            boolean flagFound = flaggedContent.stream()
                .anyMatch(f -> f[1].equals("Answer") && 
                          f[2].equals(String.valueOf(testAnswer.getAnsID())) && 
                          f[3].equals(testStaff.getUserName()) && 
                          f[5].equals(flagReason));
            assertTrue(flagFound);
        }
        
        @Test
        @DisplayName("Staff can resolve flagged content")
        void testStaffCanResolveFlaggedContent() throws SQLException {
            // Staff flags a question
            String flagReason = "This is a test flag reason for resolution";
            dbHelper3.flagContent("Question", testQuestion.getQuestionID(), testStaff.getUserName(), flagReason);
            
            // Get all flagged content
            List<String[]> flaggedContent = dbHelper3.getAllFlaggedContent();
            assertFalse(flaggedContent.isEmpty());
            
            // Find the flag ID
            String flagId = flaggedContent.stream()
                .filter(f -> f[1].equals("Question") && 
                       f[2].equals(String.valueOf(testQuestion.getQuestionID())) && 
                       f[5].equals(flagReason))
                .findFirst()
                .map(f -> f[0])
                .orElse(null);
            
            assertNotNull(flagId);
            
            // Resolve the flag
            dbHelper3.resolveFlaggedContent(Integer.parseInt(flagId));
            
            // Verify the flag is no longer in the active flags list
            List<String[]> updatedFlaggedContent = dbHelper3.getAllFlaggedContent();
            boolean flagStillExists = updatedFlaggedContent.stream()
                .anyMatch(f -> f[0].equals(flagId));
            
            assertFalse(flagStillExists);
        }
        
        /**
         * Utility method to get a new unique answer ID
         */
        private int getNewAnswerId() throws SQLException {
            List<Answer> allAnswers = dbHelper2.getAllAnswers();
            return allAnswers.stream()
                .mapToInt(Answer::getAnsID)
                .max()
                .orElse(0) + 1;
        }
    }
    
    @Nested
    @DisplayName("User Story 5 & 6: Student Restriction Features")
    class StudentRestrictionTests {

        @Test
        @DisplayName("Staff can request to restrict a student")
        void testStaffCanRequestStudentRestriction() throws SQLException {
            // Staff sends a restriction request
            String restrictionReason = "This is a test restriction reason";
            String requestMessage = "[RESTRICT REQUEST] Student: " + testStudent.getUserName() + " - " + restrictionReason;
            
            dbHelper3.insertFeedback(
                testQuestion.getQuestionID(),
                testInstructor.getUserName(),
                testStaff.getUserName(),
                requestMessage
            );
            
            // Check if the instructor received the restriction request
            List<String[]> instructorFeedback = dbHelper3.getFeedbackForUser(testInstructor.getUserName());
            
            boolean requestFound = instructorFeedback.stream()
                .anyMatch(f -> f[4].equals(requestMessage) && 
                          f[5].equals(testStaff.getUserName()));
            
            assertTrue(requestFound);
        }
        
        @Test
        @DisplayName("Instructor can restrict a student")
        void testInstructorCanRestrictStudent() throws SQLException {
            // Get original role
            String originalRole = dbHelper.getUserRole(testStudent.getUserName());
            
            // Add restriction
            String restrictedRole = originalRole + ",Restricted";
            dbHelper.updateUserRole(testStudent.getUserName(), restrictedRole);
            
            // Verify the student is now restricted
            String updatedRole = dbHelper.getUserRole(testStudent.getUserName());
            assertTrue(updatedRole.contains("Restricted"));
            
            // Clean up - restore original role
            dbHelper.updateUserRole(testStudent.getUserName(), originalRole);
        }
        
        @Test
        @DisplayName("Instructor can unrestrict a student")
        void testInstructorCanUnrestrictStudent() throws SQLException {
            // First restrict the student
            String originalRole = dbHelper.getUserRole(testStudent.getUserName());
            String restrictedRole = originalRole + ",Restricted";
            dbHelper.updateUserRole(testStudent.getUserName(), restrictedRole);
            
            // Verify the restriction was applied
            String updatedRole = dbHelper.getUserRole(testStudent.getUserName());
            assertTrue(updatedRole.contains("Restricted"));
            
            // Now unrestrict the student
            dbHelper.updateUserRole(testStudent.getUserName(), originalRole);
            
            // Verify the student is no longer restricted
            String finalRole = dbHelper.getUserRole(testStudent.getUserName());
            assertFalse(finalRole.contains("Restricted"));
        }
        
        @Test
        @DisplayName("Instructor can view all restricted students")
        void testInstructorCanViewRestrictedStudents() throws SQLException {
            // First restrict the student
            String originalRole = dbHelper.getUserRole(testStudent.getUserName());
            String restrictedRole = originalRole + ",Restricted";
            dbHelper.updateUserRole(testStudent.getUserName(), restrictedRole);
            
            // Get list of restricted users
            List<String[]> restrictedUsers = dbHelper.getRestrictedUsers();
            
            // Verify our test student is in the list
            boolean studentIsRestricted = restrictedUsers.stream()
                .anyMatch(u -> u[0].equals(testStudent.getUserName()));
            
            assertTrue(studentIsRestricted);
            
            // Clean up - restore original role
            dbHelper.updateUserRole(testStudent.getUserName(), originalRole);
        }
    }
}
