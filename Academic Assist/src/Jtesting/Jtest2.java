package Jtesting;

/*******
 * <p> Title: Jtest2 Class. </p>
 * 
 * <p> Description: A Java demonstration for JUnit Automation tests for Questions and Answers System </p>
 * 
 * <p> Copyright: © 2025 </p>
 * 
 * @author Hridaya Amol Dande
 * 
 * @version 1.00
 * 
 */

/**
 * This test package contains unit tests for the Questions and Answers System.
 * <p>
 * The following imports are used in this test class:
 * </p>
 * <ul>
 *   <li>JUnit 5 assertions - for test validations</li>
 *   <li>SQLException - for handling database exceptions</li>
 *   <li>Date and List - core Java utilities</li>
 *   <li>JUnit 5 annotations - for test lifecycle management</li>
 *   <li>DatabaseHelper and DatabaseHelper2 - database operations</li>
 *   <li>Question/Questions, Answer/Answers - core domain models and managers</li>
 * </ul>
 */

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import application.Question;
import application.Answer;
import application.Questions;
import application.Answers;
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper2;

/**
 * A test class for validating the operations of the Questions and Answers system and its database interactions.
 * <p>
 * This class contains unit tests that verify the proper functionality of operations for managing 
 * questions and answers. The tests cover the following key operations:
 * <ul>
 *   <li>Question validation and management</li>
 *   <li>Answer management and operations</li>
 *   <li>Database operations for questions and answers</li>
 *   <li>Search and retrieval functionality</li>
 * </ul>
 * <p>
 * Each test method follows a consistent pattern:
 * <ol>
 *   <li>Setting up test data</li>
 *   <li>Executing the operations being tested</li>
 *   <li>Verifying the expected outcomes</li>
 * </ol>
 * <p>
 * All tests utilize database connections that are refreshed before each test execution
 * to ensure test isolation.
 * 
 * @see application.Question
 * @see application.Questions
 * @see application.Answer
 * @see application.Answers
 * @see databasePart1.DatabaseHelper
 * @see databasePart1.DatabaseHelper2
 * @see java.sql.SQLException
 */

public class Jtest2 {
    /** Database helper for database operations */
    private DatabaseHelper dbHelper;
    
    /** Database helper for additional database operations */
    private DatabaseHelper2 dbHelper2;
    
    /** Manager for question-related operations */
    private Questions questions;
    
    /** Manager for answer-related operations */
    private Answers answers;

    /**
     * Sets up the test environment before each test.
     * <p>
     * This method initializes the database helpers, establishes database connections,
     * and initializes the Questions and Answers managers.
     * 
     * @throws SQLException if a database access error occurs
     * @see databasePart1.DatabaseHelper#connectToDatabase()
     * @see databasePart1.DatabaseHelper2#connectToDatabase()
     */
    @BeforeEach
    public void setUp() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper2 = new DatabaseHelper2();
        dbHelper.connectToDatabase();
        dbHelper2.connectToDatabase();
        questions = new Questions();
        answers = new Answers();
    }

    /**
     * Cleans up resources after each test.
     * <p>
     * This method closes the database connections to ensure proper resource management
     * and test isolation.
     * 
     * @see databasePart1.DatabaseHelper#closeConnection()
     * @see databasePart1.DatabaseHelper2#closeConnection()
     */
    @AfterEach
    public void tearDown() {
        dbHelper.closeConnection();
        dbHelper2.closeConnection();
    }

    @Nested
    @DisplayName("Question Tests")
    class QuestionTests {

        @Test
        @DisplayName("Correct question should be valid")
        void testQuestionValidation() {
            Question question = new Question(1, "What are the health benefits of green tea?", "Hridaya", new Date());
            assertTrue(question.checkValidity());
        }

        @Test
        @DisplayName("Empty question should be invalid")
        void testQuestionValidationEmptyContent() {
            Question question = new Question(1, "", "James", new Date());
            assertFalse(question.checkValidity());
        }

        @Test
        @DisplayName("Question status should track resolved/unresolved state")
        void testMarkQuestionAsAnswered() {
            Question question = new Question(1, "How do I reset my password on the website?", "Bren", new Date());
            assertFalse(question.isResolved());
            question.setResolved(true);
            assertTrue(question.isResolved());
        }
    }

    @Nested
    @DisplayName("Questions Manager Tests")
    class QuestionsManagerTests {

        @Test
        @DisplayName("Add and retrieve question")
        void testAddQuestion() {
            String uniqueContent = "How can I improve my programming skills?" + System.currentTimeMillis();
            Question question = new Question(0, uniqueContent, "Jake", new Date());
            questions.insertQuestion(question);

            List<Question> allQuestions = questions.listAllQuestions();
            boolean found = allQuestions.stream().anyMatch(q -> q.getBodyText().equals(uniqueContent));
            assertTrue(found);
        }

        @Test
        @DisplayName("Search questions by keyword")
        void testSearchQuestions() {
            String uniqueContent = "Best techniques for time management?" + System.currentTimeMillis();
            Question question = new Question(0, uniqueContent, "Alan", new Date());
            questions.insertQuestion(question);

            List<Question> results = questions.searchQuestions(uniqueContent);
            assertFalse(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("DatabaseHelper Tests")
    class DatabaseHelperTests {

        @Test
        @DisplayName("Insert and retrieve question from database")
        void testInsertQuestion() throws SQLException {
            int newId = getNewQuestionId();
            Question question = new Question(newId, "What is the difference between AI and Machine Learning?", "Uday", new Date());
            dbHelper2.insertQuestion(question);
            List<Question> questions = dbHelper2.getAllQuestions();
            assertTrue(questions.stream().anyMatch(q -> q.getQuestionID() == question.getQuestionID()));
        }

        @Test
        @DisplayName("Update question in database")
        void testUpdateQuestion() throws SQLException {
            int newId = getNewQuestionId();
            Question question = new Question(newId, "What are the fundamentals of cybersecurity?", "Hridaya", new Date());
            dbHelper2.insertQuestion(question);
            question.setBodyText("Updated: What are the basic principles of cybersecurity?");
            dbHelper2.updateQuestion(question);
            Question updatedQuestion = dbHelper2.getAllQuestions().stream()
                .filter(q -> q.getQuestionID() == question.getQuestionID())
                .findFirst()
                .orElse(null);
            assertNotNull(updatedQuestion);
            assertEquals("Updated: What are the basic principles of cybersecurity?", updatedQuestion.getBodyText());
        }

        @Test
        @DisplayName("Delete question from database")
        void testDeleteQuestion() throws SQLException {
            int newId = getNewQuestionId();
            Question question = new Question(newId, "What are the key features of Java 17?", "James", new Date());
            dbHelper2.insertQuestion(question);
            dbHelper2.deleteQuestion(question.getQuestionID());
            List<Question> questions = dbHelper2.getAllQuestions();
            assertFalse(questions.stream().anyMatch(q -> q.getQuestionID() == question.getQuestionID()));
        }

        private int getNewQuestionId() throws SQLException {
            List<Question> allQuestions = dbHelper2.getAllQuestions();
            return allQuestions.stream()
                .mapToInt(Question::getQuestionID)
                .max()
                .orElse(0) + 1;
        }
    }

    @Nested
    @DisplayName("Answers Manager Tests")
    class AnswersManagerTests {

        @Test
        @DisplayName("Add and retrieve answer")
        void testAddAnswer() {
            Answer answer = new Answer(1, 1, "You can reset your password by clicking 'Forgot Password' on the login page.", "Bren", new Date());
            answers.insertAnswer(answer);
            assertTrue(answers.listAllAnswers().contains(answer));
        }

        @Test
        @DisplayName("Delete answer")
        void testDeleteAnswer() {
            Answer answer = new Answer(1, 1, "Regular exercise can help improve mental health and reduce stress.", "Jake", new Date());
            answers.insertAnswer(answer);
            answers.deleteAnswer(answer.getAnsID());
            assertFalse(answers.listAllAnswers().contains(answer));
        }

        @Test
        @DisplayName("Modify answer")
        void testModifyAnswer() {
            Answer answer = new Answer(1, 1, "A healthy diet should include a balance of proteins, fats, and carbohydrates.", "Alan", new Date());
            answers.insertAnswer(answer);
            answer.setBodyText("Updated: A well-balanced diet should include lean proteins, healthy fats, and complex carbohydrates.");
            answers.modifyAnswer(answer);
            assertEquals("Updated: A well-balanced diet should include lean proteins, healthy fats, and complex carbohydrates.", answers.findAnswerByID(1).getBodyText());
        }

        @Test
        @DisplayName("Find answer by ID")
        void testFindAnswerByID() {
            Answer answer = new Answer(1, 1, "You should drink at least 8 cups of water per day.", "Uday", new Date());
            answers.insertAnswer(answer);
            assertNotNull(answers.findAnswerByID(1));
        }

       
    }
}
