package Jtesting;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import application.User;
import application.Question;
import application.Answer;
import application.Request;
import application.ReviewerProfile;
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;
import databasePart1.DatabaseHelper3;

/**
 * A test class for validating the implementation of the user stories:
 * <ol>
 *   <li>Students can search answered and unanswered questions using a search bar</li>
 *   <li>Students can search for reviewers using a dropdown filter</li>
 *   <li>Reviewers can display their details about their experience and background</li>
 *   <li>Reviewers can access a list of reviews given to them by the students</li>
 *   <li>Instructors can assign scores to reviewers based on specific parameters</li>
 *   <li>Students can see the assigned score for each reviewer</li>
 *   <li>Instructors, staff and admins can see a list of admin-specific actions</li>
 *   <li>An instructor and staff can request the ability to use admin-specific actions</li>
 *   <li>An admin can disable the instructor's ability to use admin-specific actions</li>
 *   <li>Admin, instructors and staff can see a list of closed requests</li>
 * </ol>
 */
public class Jtest4 {
    private DatabaseHelper dbHelper;
    private DatabaseHelper2 dbHelper2;
    private DatabaseHelper3 dbHelper3;
    
    private User testStudent;
    private User testReviewer;
    private User testInstructor;
    private User testAdmin;
    private User testStaff;
    
    private Question testQuestion;
    private Answer testAnswer;

    @BeforeEach
    public void setUp() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper2 = new DatabaseHelper2();
        dbHelper3 = new DatabaseHelper3();
        
        dbHelper.connectToDatabase();
        dbHelper2.connectToDatabase();
        dbHelper3.connectToDatabase();
        
        // Create test users with unique usernames
        String suffix = UUID.randomUUID().toString().substring(0, 5);
        
        testStudent = new User("teststudent" + suffix, "Test", "Student", "test@student.com", "password", "Student");
        testReviewer = new User("testreviewer" + suffix, "Test", "Reviewer", "test@reviewer.com", "password", "Reviewer");
        testInstructor = new User("testinstructor" + suffix, "Test", "Instructor", "test@instructor.com", "password", "Instructor");
        testAdmin = new User("testadmin" + suffix, "Test", "Admin", "test@admin.com", "password", "Admin");
        testStaff = new User("teststaff" + suffix, "Test", "Staff", "test@staff.com", "password", "Staff");
        
        // Register test users if they don't exist
        if (!dbHelper.doesUserExist(testStudent.getUserName())) {
            dbHelper.register(testStudent);
        }
        
        if (!dbHelper.doesUserExist(testReviewer.getUserName())) {
            dbHelper.register(testReviewer);
        }
        
        if (!dbHelper.doesUserExist(testInstructor.getUserName())) {
            dbHelper.register(testInstructor);
        }
        
        if (!dbHelper.doesUserExist(testAdmin.getUserName())) {
            dbHelper.register(testAdmin);
        }
        
        if (!dbHelper.doesUserExist(testStaff.getUserName())) {
            dbHelper.register(testStaff);
        }
        
        // Create a test question
        int questionId = getNewQuestionId();
        testQuestion = new Question(questionId, "Test question for user stories", testStudent.getUserName(), new Date());
        dbHelper2.insertQuestion(testQuestion);
        
        // Create a test answer
        int answerId = getNewAnswerId();
        testAnswer = new Answer(answerId, testQuestion.getQuestionID(), "Test answer for user stories", testReviewer.getUserName(), new Date());
        dbHelper2.insertAnswer(testAnswer);
    }

    @AfterEach
    public void tearDown() {
        try {
            // Clean up test data
            if (testQuestion != null) {
                dbHelper2.deleteQuestion(testQuestion.getQuestionID());
            }
            
            // Close all connections
            dbHelper.closeConnection();
            dbHelper2.closeConnection();
            dbHelper3.closeConnection();
        } catch (SQLException e) {
            // Ignore errors during cleanup
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

    @Nested
    @DisplayName("User Story 1: Student Question Search")
    class StudentQuestionSearchTests {

        @Test
        @DisplayName("Students can search questions by keyword")
        void testSearchQuestionsByKeyword() throws SQLException {
            // Add a question with a unique keyword
            String uniqueKeyword = "unique" + UUID.randomUUID().toString().substring(0, 5);
            Question testQuestionWithKeyword = new Question(
                getNewQuestionId(),
                "Test question with " + uniqueKeyword + " keyword",
                testStudent.getUserName(),
                new Date()
            );
            dbHelper2.insertQuestion(testQuestionWithKeyword);
            
            // Search for the question by keyword
            List<Question> searchResults = dbHelper3.searchQuestions(uniqueKeyword, "All", null);
            
            // Verify the question is found
            assertNotNull(searchResults);
            assertFalse(searchResults.isEmpty());
            assertTrue(searchResults.stream()
                .anyMatch(q -> q.getQuestionID() == testQuestionWithKeyword.getQuestionID()));
            
            // Clean up
            dbHelper2.deleteQuestion(testQuestionWithKeyword.getQuestionID());
        }
        
        @Test
        @DisplayName("Students can filter questions by answer status")
        void testFilterQuestionsByAnswerStatus() throws SQLException {
            // Get a question without answers
            Question unansweredQuestion = new Question(
                getNewQuestionId(),
                "Unanswered test question",
                testStudent.getUserName(),
                new Date()
            );
            dbHelper2.insertQuestion(unansweredQuestion);
            
            // Create an answered question
            Question answeredQuestion = new Question(
                getNewQuestionId(),
                "Answered test question",
                testStudent.getUserName(),
                new Date()
            );
            dbHelper2.insertQuestion(answeredQuestion);
            
            // Add an answer to the answered question
            Answer answer = new Answer(
                getNewAnswerId(),
                answeredQuestion.getQuestionID(),
                "This is a test answer",
                testReviewer.getUserName(),
                new Date()
            );
            dbHelper2.insertAnswer(answer);
            
            // Search for questions with answers
            List<Question> answeredQuestions = dbHelper3.searchQuestions("", "Answered", null);
            assertTrue(answeredQuestions.stream()
                .anyMatch(q -> q.getQuestionID() == answeredQuestion.getQuestionID()));
            
            // Search for questions without answers
            List<Question> unansweredQuestions = dbHelper3.searchQuestions("", "Unanswered", null);
            assertTrue(unansweredQuestions.stream()
                .anyMatch(q -> q.getQuestionID() == unansweredQuestion.getQuestionID()));
            
            // Clean up
            dbHelper2.deleteQuestion(unansweredQuestion.getQuestionID());
            dbHelper2.deleteQuestion(answeredQuestion.getQuestionID());
        }
    }

    @Nested
    @DisplayName("User Story 2: Student Reviewer Search")
    class StudentReviewerSearchTests {

        @Test
        @DisplayName("Students can search for questions by reviewer")
        void testSearchQuestionsByReviewer() throws SQLException {
            // Create a test question review by a specific reviewer
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0, // answerID = 0 for question review
                testReviewer.getUserName(),
                "This is a test review",
                new Date()
            );
            
            // Search for questions by reviewer
            List<Question> reviewerQuestions = dbHelper3.searchQuestions("", "Reviewer", testReviewer.getUserName());
            
            // Verify the question is found
            assertNotNull(reviewerQuestions);
            assertFalse(reviewerQuestions.isEmpty());
            assertTrue(reviewerQuestions.stream()
                .anyMatch(q -> q.getQuestionID() == testQuestion.getQuestionID()));
        }
        
        @Test
        @DisplayName("Students can get a list of available reviewers")
        void testGetAvailableReviewers() throws SQLException {
            // Insert a review to ensure the reviewer is in the list
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                testReviewer.getUserName(),
                "This is a test review",
                new Date()
            );
            
            // Get the list of reviewers
            List<String> reviewers = dbHelper3.getAllReviewers();
            
            // Verify our test reviewer is in the list
            assertNotNull(reviewers);
            assertTrue(reviewers.contains(testReviewer.getUserName()));
        }
    }

    @Nested
    @DisplayName("User Story 3: Reviewer Profile Management")
    class ReviewerProfileTests {

        @Test
        @DisplayName("Reviewers can create their profile with experience and background")
        void testReviewerCanCreateProfile() throws SQLException {
            // Create profile details
            String experience = "5 years of experience in computer science";
            String background = "PhD in Computer Science";
            
            // Update the reviewer's profile
            dbHelper3.updateReviewerProfile(testReviewer.getUserName(), experience, background);
            
            // Retrieve the profile
            ReviewerProfile profile = dbHelper3.getReviewerProfile(testReviewer.getUserName());
            
            // Verify the profile details
            assertNotNull(profile);
            assertEquals(experience, profile.getExperience());
            assertEquals(background, profile.getBackground());
        }
        
        @Test
        @DisplayName("Reviewers can update their existing profile")
        void testReviewerCanUpdateProfile() throws SQLException {
            // First create a profile
            String initialExperience = "Initial experience";
            String initialBackground = "Initial background";
            dbHelper3.updateReviewerProfile(testReviewer.getUserName(), initialExperience, initialBackground);
            
            // Now update the profile
            String updatedExperience = "Updated experience";
            String updatedBackground = "Updated background";
            dbHelper3.updateReviewerProfile(testReviewer.getUserName(), updatedExperience, updatedBackground);
            
            // Retrieve and verify the updated profile
            ReviewerProfile profile = dbHelper3.getReviewerProfile(testReviewer.getUserName());
            assertNotNull(profile);
            assertEquals(updatedExperience, profile.getExperience());
            assertEquals(updatedBackground, profile.getBackground());
        }
    }

    @Nested
    @DisplayName("User Story 4: Reviewer Feedback Access")
    class ReviewerFeedbackTests {

        @Test
        @DisplayName("Reviewers can view feedback from students")
        void testReviewerCanViewFeedback() throws SQLException {
            // Create a test review
            String uniqueReviewText = "Test review " + UUID.randomUUID().toString().substring(0, 5);
            
            // Insert review (returns void, not an ID)
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                testReviewer.getUserName(),
                uniqueReviewText,
                new Date()
            );
            
            // Get the review ID using a query to find our just-inserted review
            int reviewId = -1;
            List<String[]> reviews = dbHelper3.getQuestionReviewsWithIDs();
            for (String[] review : reviews) {
                if (review[3].equals(uniqueReviewText) && review[4].equals(testReviewer.getUserName())) {
                    reviewId = Integer.parseInt(review[0]);
                    break;
                }
            }
            assertTrue(reviewId > 0, "Review ID should be found");
            
            // Student submits feedback on the review
            String feedbackText = "This is test feedback on the review";
            dbHelper3.insertReviewFeedback(
                reviewId,
                testQuestion.getQuestionID(),
                false, // Not an answer review
                testStudent.getUserName(),
                testReviewer.getUserName(),
                feedbackText
            );
            
            // Get the reviewer's feedback
            List<String[]> feedback = dbHelper3.getReviewerFeedback(testReviewer.getUserName());
            
            // Verify the feedback is visible
            assertNotNull(feedback);
            assertFalse(feedback.isEmpty());
            boolean feedbackFound = feedback.stream()
                .anyMatch(f -> f[1].equals(feedbackText) && f[2].equals(testStudent.getUserName()));
            assertTrue(feedbackFound);
        }
        
        @Test
        @DisplayName("Reviewers can see all reviews they have given")
        void testReviewerCanSeeTheirReviews() throws SQLException {
            // Create multiple test reviews by this reviewer
            String reviewText = "Test review " + UUID.randomUUID().toString().substring(0, 5);
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                testReviewer.getUserName(),
                reviewText,
                new Date()
            );
            
            // Get all question reviews by this reviewer
            List<String[]> reviews = dbHelper3.getQuestionReviewsWithIDs();
            
            // Verify the review is in the list
            assertNotNull(reviews);
            assertFalse(reviews.isEmpty());
            boolean reviewFound = reviews.stream()
                .anyMatch(r -> r[3].equals(reviewText) && r[4].equals(testReviewer.getUserName()));
            assertTrue(reviewFound);
        }
    }

    @Nested
    @DisplayName("User Story 5: Instructor Reviewer Scoring")
    class InstructorReviewerScoringTests {

        @Test
        @DisplayName("Instructors can assign scores to reviewers")
        void testInstructorCanAssignScoresToReviewers() throws SQLException {
            // Instructor assigns a weight/score to a reviewer
            int score = 4; // On a scale of 1-5
            dbHelper3.setReviewerWeight(testInstructor.getUserName(), testReviewer.getUserName(), score);
            
            // Verify the score was saved
            int savedScore = dbHelper3.getReviewerWeight(testInstructor.getUserName(), testReviewer.getUserName());
            assertEquals(score, savedScore);
        }
        
        @Test
        @DisplayName("Instructors can update reviewer scores")
        void testInstructorCanUpdateReviewerScores() throws SQLException {
            // First assign an initial score
            int initialScore = 3;
            dbHelper3.setReviewerWeight(testInstructor.getUserName(), testReviewer.getUserName(), initialScore);
            
            // Update the score
            int updatedScore = 5;
            dbHelper3.setReviewerWeight(testInstructor.getUserName(), testReviewer.getUserName(), updatedScore);
            
            // Verify the score was updated
            int savedScore = dbHelper3.getReviewerWeight(testInstructor.getUserName(), testReviewer.getUserName());
            assertEquals(updatedScore, savedScore);
        }
        
        @Test
        @DisplayName("Instructors can provide feedback with scores")
        void testInstructorCanProvideFeedbackWithScores() throws SQLException {
            // First, insert a review since insertFeedbackForReviewer requires an existing review
            String uniqueReviewText = "Instructor feedback test review " + UUID.randomUUID().toString().substring(0, 5);
            
            // Add a review from this reviewer to ensure the method can find a review to link feedback to
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,  // answerID = 0 for question review
                testReviewer.getUserName(),
                uniqueReviewText,
                new Date()
            );
            
            // Assign a score
            int score = 4;
            dbHelper3.setReviewerWeight(testInstructor.getUserName(), testReviewer.getUserName(), score);
            
            // Provide feedback
            String feedbackText = "Good work, but room for improvement";
            dbHelper3.insertFeedbackForReviewer(
                testReviewer.getUserName(),
                testInstructor.getUserName(),
                feedbackText
            );
            
            // Get the reviewer's feedback
            List<String[]> feedback = dbHelper3.getReviewerFeedback(testReviewer.getUserName());
            
            // Verify the feedback is visible
            assertNotNull(feedback);
            assertFalse(feedback.isEmpty());
            
            // Debug: Print feedback entries
            System.out.println("Feedback entries: " + feedback.size());
            for (String[] entry : feedback) {
                System.out.println("Entry: Type=" + entry[0] + ", Text=" + entry[1] + ", From=" + entry[2]);
            }
            
            boolean feedbackFound = feedback.stream()
                .anyMatch(f -> f[1].contains(feedbackText) || (f.length > 2 && f[2].equals(testInstructor.getUserName())));
            assertTrue(feedbackFound, "Feedback from instructor should be found");
        }
    }

    @Nested
    @DisplayName("User Story 6: Student Reviewer Score Visibility")
    class StudentReviewerScoreTests {

        @Test
        @DisplayName("Students can see reviewer scores")
        void testStudentsCanSeeReviewerScores() throws SQLException {
            // First create a review from this reviewer
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                testReviewer.getUserName(),
                "Profile test review",
                new Date()
            );
            
            // Set up a profile for the reviewer
            dbHelper3.updateReviewerProfile(
                testReviewer.getUserName(),
                "Test experience",
                "Test background"
            );
            
            // Instructor assigns a score to the reviewer
            int score = 4;
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testReviewer.getUserName(), score);
            
            // Get the reviewer's profile (which includes average rating)
            ReviewerProfile profile = dbHelper3.getReviewerProfile(testReviewer.getUserName());
            
            // Debug information
            System.out.println("Reviewer: " + testReviewer.getUserName());
            System.out.println("Profile: " + (profile != null ? profile.getUserName() : "null"));
            System.out.println("Average rating: " + (profile != null ? profile.getAverageRating() : "null"));
            
            // Verify the profile exists and has the right username
            assertNotNull(profile);
            assertEquals(testReviewer.getUserName(), profile.getUserName());
            assertTrue(profile.getAverageRating() > 0, "Average rating should be greater than zero");
        }
        
        @Test
        @DisplayName("Students can see multiple reviewer scores")
        void testStudentsCanSeeMultipleReviewerScores() throws SQLException {
            // First create a review from this reviewer
            dbHelper3.insertReview(
                testQuestion.getQuestionID(),
                0,
                testReviewer.getUserName(),
                "Multiple scores test review",
                new Date()
            );
            
            // Set up a profile for the reviewer
            dbHelper3.updateReviewerProfile(
                testReviewer.getUserName(),
                "Test experience for multiple scores",
                "Test background for multiple scores"
            );
            
            // Multiple instructors assign scores
            dbHelper3.setReviewerWeight(testInstructor.getUserName(), testReviewer.getUserName(), 4);
            
            // Create another instructor
            String suffix = UUID.randomUUID().toString().substring(0, 5);
            User secondInstructor = new User(
                "testinstructor2" + suffix,
                "Test2",
                "Instructor",
                "test2@instructor.com",
                "password",
                "Instructor"
            );
            dbHelper.register(secondInstructor);
            
            dbHelper3.setReviewerWeight(secondInstructor.getUserName(), testReviewer.getUserName(), 5);
            
            // Assign weight from student perspective as well
            dbHelper3.setReviewerWeight(testStudent.getUserName(), testReviewer.getUserName(), 3);
            
            // Get the reviewer weights for the student
            List<String[]> weights = dbHelper.getReviewerWeights(testStudent.getUserName());
            
            // Debug information
            System.out.println("Weights count: " + weights.size());
            for (String[] weight : weights) {
                System.out.println("Weight: Reviewer=" + weight[0] + ", Score=" + weight[1]);
            }
            
            // The reviewer should appear in the weights list
            assertNotNull(weights);
            boolean reviewerFound = weights.stream()
                .anyMatch(w -> w[0].equals(testReviewer.getUserName()));
            assertTrue(reviewerFound, "The reviewer should be in the student's weights list");
        }
    }

    @Nested
    @DisplayName("User Story 7: Admin Actions Visibility")
    class AdminActionsVisibilityTests {
        
        @Test
        @DisplayName("Instructors can see admin action list")
        void testInstructorsCanSeeAdminActions() throws SQLException {
            // Check if the user exists in the database
            assertTrue(dbHelper.doesUserExist(testInstructor.getUserName()));
            
            // Verify the user has Instructor role
            String role = dbHelper.getUserRole(testInstructor.getUserName());
            assertTrue(role.contains("Instructor"));
            
            // We can't directly test UI, but we can verify the database has the right structure
            // for admin requests table which is used to display admin actions
            try {
                // This query will throw an exception if the table doesn't exist
                dbHelper.getAdminAccessRequests();
                // If we get here, the query succeeded
                assertTrue(true);
            } catch (SQLException e) {
                fail("Admin access requests table doesn't exist: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Staff can see admin action list")
        void testStaffCanSeeAdminActions() throws SQLException {
            // Check if the user exists in the database
            assertTrue(dbHelper.doesUserExist(testStaff.getUserName()));
            
            // Verify the user has Staff role
            String role = dbHelper.getUserRole(testStaff.getUserName());
            assertTrue(role.contains("Staff"));
            
            // Same check as above - verify the database structure supports admin actions
            try {
                dbHelper.getAdminAccessRequests();
                assertTrue(true);
            } catch (SQLException e) {
                fail("Admin access requests table doesn't exist: " + e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("User Story 8: Admin Access Request")
    class AdminAccessRequestTests {
        
        @Test
        @DisplayName("Instructors can request admin access")
        void testInstructorsCanRequestAdminAccess() throws SQLException {
            // Instructor submits a request for admin access
            String reason = "Need admin access for course management";
            dbHelper.submitAdminAccessRequest(testInstructor.getUserName(), reason);
            
            // Verify the request exists
            boolean hasPendingRequest = dbHelper.hasUserRequestedAdminAccess(testInstructor.getUserName());
            assertTrue(hasPendingRequest);
        }
        
        @Test
        @DisplayName("Staff can request admin access")
        void testStaffCanRequestAdminAccess() throws SQLException {
            // Staff submits a request for admin access
            String reason = "Need admin access for system maintenance";
            dbHelper.submitAdminAccessRequest(testStaff.getUserName(), reason);
            
            // Verify the request exists
            boolean hasPendingRequest = dbHelper.hasUserRequestedAdminAccess(testStaff.getUserName());
            assertTrue(hasPendingRequest);
        }
        
        @Test
        @DisplayName("Admin can approve admin access requests")
        void testAdminCanApproveAdminAccessRequests() throws SQLException {
            // Staff submits a request
            String reason = "Need admin access for testing";
            dbHelper.submitAdminAccessRequest(testStaff.getUserName(), reason);
            
            // Admin approves the request
            dbHelper.approveAdminAccessRequest(testStaff.getUserName());
            
            // Verify the request was approved
            boolean requestApproved = dbHelper.isUserRequestApproved(testStaff.getUserName());
            assertTrue(requestApproved);
            
            // Verify the user role was updated (may need to re-fetch the user)
            String updatedRole = dbHelper.getUserRole(testStaff.getUserName());
            assertTrue(updatedRole.contains("Admin"));
        }
    }

    @Nested
    @DisplayName("User Story 9: Admin Access Revocation")
    class AdminAccessRevocationTests {
        
        @Test
        @DisplayName("Admin can disable another user's admin privileges")
        void testAdminCanDisableAdminPrivileges() throws SQLException {
            // First give the instructor admin privileges
            String originalRole = dbHelper.getUserRole(testInstructor.getUserName());
            String adminRole = originalRole + ",Admin";
            dbHelper.updateUserRole(testInstructor.getUserName(), adminRole);
            
            // Verify the instructor now has admin privileges
            String updatedRole = dbHelper.getUserRole(testInstructor.getUserName());
            assertTrue(updatedRole.contains("Admin"));
            
            // Now the admin removes these privileges
            String nonAdminRole = updatedRole.replace(",Admin", "");
            dbHelper.updateUserRole(testInstructor.getUserName(), nonAdminRole);
            
            // Add to closed requests
            String reason = "Admin privileges disabled by test";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Verify the admin privileges were removed
            String finalRole = dbHelper.getUserRole(testInstructor.getUserName());
            assertFalse(finalRole.contains("Admin"));
        }
        
        @Test
        @DisplayName("Disabled admin access is recorded in closed requests")
        void testDisabledAdminAccessIsRecorded() throws SQLException {
            // Add a closed request
            String reason = "Admin privileges disabled for testing";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests
            var closedRequests = dbHelper.getClosedAdminRequests();
            
            // Verify our closed request is in the list
            boolean requestFound = false;
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    closedRequests.get(i).getReason().equals(reason)) {
                    requestFound = true;
                    break;
                }
            }
            assertTrue(requestFound);
        }
    }

    @Nested
    @DisplayName("User Story 10: Closed Requests Visibility")
    class ClosedRequestsVisibilityTests {
        
        @Test
        @DisplayName("Admin can view closed requests")
        void testAdminCanViewClosedRequests() throws SQLException {
            // Add a closed request
            String reason = "Test closed request for admin view";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests
            var closedRequests = dbHelper.getClosedAdminRequests();
            
            // Verify the list is not empty
            assertNotNull(closedRequests);
            assertFalse(closedRequests.isEmpty());
        }
        
        @Test
        @DisplayName("Instructors can view their closed requests")
        void testInstructorsCanViewTheirClosedRequests() throws SQLException {
            // Add a closed request for this instructor
            String reason = "Test closed request for instructor view";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests (the filter for user's own requests is done in the UI)
            var closedRequests = dbHelper.getClosedAdminRequests();
            
            // Verify the list includes the instructor's request
            boolean requestFound = false;
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    closedRequests.get(i).getReason().equals(reason)) {
                    requestFound = true;
                    break;
                }
            }
            assertTrue(requestFound);
        }
        
        @Test
        @DisplayName("Staff can view their closed requests")
        void testStaffCanViewTheirClosedRequests() throws SQLException {
            // Add a closed request for this staff member
            String reason = "Test closed request for staff view";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testStaff.getUserName(), reason, currentDate);
            
            // Get closed requests
            var closedRequests = dbHelper.getClosedAdminRequests();
            
            // Verify the list includes the staff member's request
            boolean requestFound = false;
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testStaff.getUserName()) &&
                    closedRequests.get(i).getReason().equals(reason)) {
                    requestFound = true;
                    break;
                }
            }
            assertTrue(requestFound);
        }
    }

    @Nested
    @DisplayName("User Story 11: Instructor Request Reopening")
    class InstructorRequestReopeningTests {
        
        @Test
        @DisplayName("Instructors can update description of a closed request")
        void testInstructorCanUpdateRequestDescription() throws SQLException {
            // Add a closed request for this instructor
            String reason = "Original request reason";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests to find the ID
            var closedRequests = dbHelper.getClosedAdminRequests();
            int requestId = -1;
            
            // Find our test request
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    closedRequests.get(i).getDescription().equals(reason)) {
                    requestId = closedRequests.get(i).getId();
                    break;
                }
            }
            
            assertTrue(requestId > 0, "Request ID should be found");
            
            // Update the description
            String updatedDescription = "Updated request reason";
            dbHelper.updateAdminRequestDescription(requestId, updatedDescription);
            
            // Get the requests again and verify the description was updated
            closedRequests = dbHelper.getClosedAdminRequests();
            boolean descriptionUpdated = false;
            
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getId() == requestId) {
                    assertEquals(updatedDescription, closedRequests.get(i).getDescription());
                    descriptionUpdated = true;
                    break;
                }
            }
            
            assertTrue(descriptionUpdated, "Description should be updated");
        }
        
        @Test
        @DisplayName("Instructors can reopen a closed request")
        void testInstructorCanReopenRequest() throws SQLException {
            // Add a closed request for this instructor
            String reason = "Closed request to reopen";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests to find the ID
            var closedRequests = dbHelper.getClosedAdminRequests();
            int requestId = -1;
            
            // Find our test request
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    closedRequests.get(i).getDescription().equals(reason)) {
                    requestId = closedRequests.get(i).getId();
                    break;
                }
            }
            
            assertTrue(requestId > 0, "Request ID should be found");
            
            // Reopen the request
            dbHelper.reopenAdminRequest(requestId);
            
            // Verify the request appears in pending requests
            var pendingRequests = dbHelper.getAdminAccessRequests();
            boolean reopenedRequestFound = false;
            int reopenedId = -1;
            
            for (int i = 0; i < pendingRequests.size(); i++) {
                if (pendingRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    pendingRequests.get(i).getDescription().contains("Reopened: " + reason)) {
                    reopenedRequestFound = true;
                    reopenedId = pendingRequests.get(i).getId();
                    break;
                }
            }
            
            assertTrue(reopenedRequestFound, "Reopened request should be found in pending requests");
            assertTrue(reopenedId > 0, "Reopened request ID should be valid");
        }
        
        @Test
        @DisplayName("Reopened request description is prefixed with 'Reopened:'")
        void testReopenedRequestHasPrefixInDescription() throws SQLException {
            // Add a closed request for this instructor
            String reason = "Request with reason to check prefix";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests to find the ID
            var closedRequests = dbHelper.getClosedAdminRequests();
            int requestId = -1;
            
            // Find our test request
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    closedRequests.get(i).getDescription().equals(reason)) {
                    requestId = closedRequests.get(i).getId();
                    break;
                }
            }
            
            assertTrue(requestId > 0, "Request ID should be found");
            
            // Reopen the request
            dbHelper.reopenAdminRequest(requestId);
            
            // Verify the reopened request has the prefix
            var pendingRequests = dbHelper.getAdminAccessRequests();
            boolean hasPrefix = false;
            
            for (int i = 0; i < pendingRequests.size(); i++) {
                if (pendingRequests.get(i).getUsername().equals(testInstructor.getUserName())) {
                    String desc = pendingRequests.get(i).getDescription();
                    if (desc.startsWith("Reopened: ") && desc.contains(reason)) {
                        hasPrefix = true;
                        break;
                    }
                }
            }
            
            assertTrue(hasPrefix, "Reopened request description should be prefixed with 'Reopened:'");
        }
    }
    
    @Nested
    @DisplayName("User Story 12: Reopened Request Linking")
    class ReopenedRequestLinkingTests {
        
        @Test
        @DisplayName("Reopened request has reference to original request ID")
        void testReopenedRequestHasReferenceToOriginal() throws SQLException {
            // Add a closed request for this instructor
            String reason = "Original request for linking test";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests to find the ID
            var closedRequests = dbHelper.getClosedAdminRequests();
            int originalId = -1;
            
            // Find our test request
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    closedRequests.get(i).getDescription().equals(reason)) {
                    originalId = closedRequests.get(i).getId();
                    break;
                }
            }
            
            assertTrue(originalId > 0, "Original request ID should be found");
            
            // Reopen the request
            dbHelper.reopenAdminRequest(originalId);
            
            // Get the reopened request and check it has a reference to the original
            var pendingRequests = dbHelper.getAdminAccessRequests();
            boolean hasReference = false;
            
            for (int i = 0; i < pendingRequests.size(); i++) {
                if (pendingRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    pendingRequests.get(i).getDescription().contains(reason)) {
                    // Check if reopenedFromId is set to the original ID
                    if (pendingRequests.get(i).getReopenedFromId() == originalId) {
                        hasReference = true;
                        break;
                    }
                }
            }
            
            assertTrue(hasReference, "Reopened request should have reference to original request ID");
        }
        
        @Test
        @DisplayName("System can retrieve original request from reopened request")
        void testSystemCanRetrieveOriginalRequest() throws SQLException {
            // Add a closed request for this instructor
            String reason = "Original request to retrieve";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests to find the ID
            var closedRequests = dbHelper.getClosedAdminRequests();
            int originalId = -1;
            
            // Find our test request
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    closedRequests.get(i).getDescription().equals(reason)) {
                    originalId = closedRequests.get(i).getId();
                    break;
                }
            }
            
            assertTrue(originalId > 0, "Original request ID should be found");
            
            // Reopen the request
            dbHelper.reopenAdminRequest(originalId);
            
            // Get the reopened request
            var pendingRequests = dbHelper.getAdminAccessRequests();
            int reopenedFromId = -1;
            
            for (int i = 0; i < pendingRequests.size(); i++) {
                if (pendingRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    pendingRequests.get(i).getDescription().contains(reason)) {
                    reopenedFromId = pendingRequests.get(i).getReopenedFromId();
                    break;
                }
            }
            
            assertTrue(reopenedFromId > 0, "Reopened request should have a valid reopenedFromId");
            assertEquals(originalId, reopenedFromId, "Reopened request should reference the original request ID");
            
            // Try to retrieve the original request
            Request originalRequest = dbHelper.getAdminRequestById(reopenedFromId);
            assertNotNull(originalRequest, "Original request should be retrievable");
            assertEquals(testInstructor.getUserName(), originalRequest.getUsername(), "Original request should have correct username");
            assertEquals(reason, originalRequest.getDescription(), "Original request should have correct description");
        }
        
        @Test
        @DisplayName("Reopened request isReopened flag is true")
        void testReopenedRequestFlagIsTrue() throws SQLException {
            // Add a closed request for this instructor
            String reason = "Request to test isReopened flag";
            String currentDate = java.time.LocalDate.now().toString();
            dbHelper.addClosedAdminRequest(testInstructor.getUserName(), reason, currentDate);
            
            // Get closed requests to find the ID
            var closedRequests = dbHelper.getClosedAdminRequests();
            int originalId = -1;
            
            // Find our test request
            for (int i = 0; i < closedRequests.size(); i++) {
                if (closedRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    closedRequests.get(i).getDescription().equals(reason)) {
                    originalId = closedRequests.get(i).getId();
                    break;
                }
            }
            
            assertTrue(originalId > 0, "Original request ID should be found");
            
            // Reopen the request
            dbHelper.reopenAdminRequest(originalId);
            
            // Get the reopened request and check its isReopened flag
            var pendingRequests = dbHelper.getAdminAccessRequests();
            boolean flagIsTrue = false;
            
            for (int i = 0; i < pendingRequests.size(); i++) {
                if (pendingRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    pendingRequests.get(i).getDescription().contains(reason)) {
                    if (pendingRequests.get(i).isReopened()) {
                        flagIsTrue = true;
                        break;
                    }
                }
            }
            
            assertTrue(flagIsTrue, "isReopened flag should be true for reopened requests");
        }
        
        @Test
        @DisplayName("Regular requests isReopened flag is false")
        void testRegularRequestFlagIsFalse() throws SQLException {
            // Submit a new admin access request
            String reason = "New regular request for flag test";
            dbHelper.submitAdminAccessRequest(testInstructor.getUserName(), reason);
            
            // Get pending requests
            var pendingRequests = dbHelper.getAdminAccessRequests();
            boolean foundRegularRequest = false;
            boolean flagIsFalse = false;
            
            // Find our regular request
            for (int i = 0; i < pendingRequests.size(); i++) {
                if (pendingRequests.get(i).getUsername().equals(testInstructor.getUserName()) &&
                    pendingRequests.get(i).getDescription().equals(reason)) {
                    foundRegularRequest = true;
                    // Check that isReopened is false
                    flagIsFalse = !pendingRequests.get(i).isReopened();
                    break;
                }
            }
            
            assertTrue(foundRegularRequest, "Regular request should be found");
            assertTrue(flagIsFalse, "isReopened flag should be false for regular requests");
        }
    }
}
