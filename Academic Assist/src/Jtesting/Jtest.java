package Jtesting;

/*******
 * <p> Title: Jtest Class. </p>
 * 
 * <p> Description: A Java demonstration for JUnit Automation tests for User Validation </p>
 * 
 * <p> Copyright: © 2025 </p>
 * 
 * @author Hridaya Amol Dande
 * 
 * @version 1.00
 * 
 */

/**
 * This test package contains unit tests for the User Validation System.
 * <p>
 * The following imports are used in this test class:
 * </p>
 * <ul>
 *   <li>JUnit 5 assertions - for test validations</li>
 *   <li>NameRecognizer - for validating user names</li>
 *   <li>EmailRecognizer - for validating email addresses</li>
 *   <li>UserNameRecognizer - for validating usernames</li>
 *   <li>PasswordEvaluator - for validating passwords</li>
 * </ul>
 */

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import application.NameRecognizer;
import application.PasswordEvaluator;
import application.EmailRecognizer;
import application.UserNameRecognizer;

/**
 * A test class for validating user input validation operations.
 * <p>
 * This class contains unit tests that verify the proper functionality of input validation for:
 * <ul>
 *   <li>User names (first and last)</li>
 *   <li>Email addresses</li>
 *   <li>Usernames</li>
 *   <li>Passwords</li>
 * </ul>
 * <p>
 * Each test method follows a consistent pattern:
 * <ol>
 *   <li>Setting up test input</li>
 *   <li>Executing the validation</li>
 *   <li>Verifying the expected validation results</li>
 * </ol>
 * 
 * @see application.NameRecognizer
 * @see application.EmailRecognizer
 * @see application.UserNameRecognizer
 * @see application.PasswordEvaluator
 */
public class Jtest {

    /**
     * Tests the name evaluator with valid first and last names.
     * <p>
     * This test verifies that the name evaluator correctly accepts valid names
     * and returns an empty string indicating no errors.
     * 
     * @see application.NameRecognizer#nameEvaluator(String, String)
     */
    @Test
    public void testNameEvaluatorValid() {
        String result = NameRecognizer.nameEvaluator("John", "Doe");
        assertEquals("", result, "Valid names should return an empty string.");
    }

    /**
     * Tests the name evaluator with null values.
     * <p>
     * This test verifies that the name evaluator correctly identifies null values
     * and returns an appropriate error message.
     * 
     * @see application.NameRecognizer#nameEvaluator(String, String)
     */
    @Test
    public void testNameEvaluatorNull() {
        String result = NameRecognizer.nameEvaluator(null, "Doe");
        assertEquals("Error: First name and Last name cannot be null.", result,
                "A null first name should return an error message.");
        result = NameRecognizer.nameEvaluator("John", null);
        assertEquals("Error: First name and Last name cannot be null.", result,
                "A null last name should return an error message.");
    }

    /**
     * Test names that exceed the maximum allowed length (30 characters).
     */
    @Test
    public void testNameEvaluatorTooLong() {
        // Create a 31-character first name.
        String longFirstName = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"; // 31 A's
        String result = NameRecognizer.nameEvaluator(longFirstName, "Doe");
        assertEquals("Error: First name exceeds 30 characters.", result,
                "A first name longer than 30 characters should return an error.");

        // Create a 31-character last name.
        String longLastName = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"; // 31 B's
        result = NameRecognizer.nameEvaluator("John", longLastName);
        assertEquals("Error: Last name exceeds 30 characters.", result,
                "A last name longer than 30 characters should return an error.");
    }

    /**
     * Test names with invalid characters (anything other than letters and spaces).
     */
    @Test
    public void testNameEvaluatorInvalidCharacters() {
        String result = NameRecognizer.nameEvaluator("John1", "Doe");
        assertEquals("Error: First name contains invalid characters. Only alphabets and spaces are allowed.", result,
                "A first name with digits should return an error.");
        result = NameRecognizer.nameEvaluator("John", "Doe!");
        assertEquals("Error: Last name contains invalid characters. Only alphabets and spaces are allowed.", result,
                "A last name with invalid characters should return an error.");
    }
    
    
    // *******************************
    // Tests for EmailRecognizer.emailEvaluator
    // *******************************

    /**
     * Test a valid email address.
     */
    @Test
    public void testEmailEvaluatorValid() {
        String result = EmailRecognizer.emailEvaluator("user@example.com");
        assertEquals("", result, "A valid email should return an empty string.");
    }

    /**
     * Test email addresses that are null or empty.
     */
    @Test
    public void testEmailEvaluatorNullOrEmpty() {
        String result = EmailRecognizer.emailEvaluator("");
        assertEquals("Error: Email cannot be null or empty.", result,
                "An empty email should return an error.");
        result = EmailRecognizer.emailEvaluator(null);
        assertEquals("Error: Email cannot be null or empty.", result,
                "A null email should return an error.");
    }

    /**
     * Test an email containing invalid characters.
     */
    @Test
    public void testEmailEvaluatorInvalidCharacters() {
        String result = EmailRecognizer.emailEvaluator("user@ex!ample.com");
        assertEquals("Error: Email contains invalid characters. Only alphabets, numbers, '@', and '.' are allowed.", result,
                "An email with an invalid character should return an error.");
    }

    /**
     * Test an email missing the '@' character.
     */
    @Test
    public void testEmailEvaluatorMissingAt() {
        String result = EmailRecognizer.emailEvaluator("userexample.com");
        assertEquals("Error: Email must contain '@'.", result,
                "An email missing '@' should return an error.");
    }

    /**
     * Test an email missing the '.' character.
     */
    @Test
    public void testEmailEvaluatorMissingDot() {
        String result = EmailRecognizer.emailEvaluator("user@examplecom");
        assertEquals("Error: Email must contain '.'.", result,
                "An email missing '.' should return an error.");
    }

    /**
     * Test an email that does not contain any alphabetic character.
     */
    @Test
    public void testEmailEvaluatorMissingAlphabet() {
        String result = EmailRecognizer.emailEvaluator("12345@6789.0");
        assertEquals("Error: Email must contain at least one alphabet.", result,
                "An email without any letters should return an error.");
    }
    
    
    // *******************************
    // Tests for UserNameRecognizer.checkForValidUserName
    // *******************************

    /**
     * Test a username that starts with a non-alphabetic character.
     */
    @Test
    public void testUserNameRecognizerInvalidStart() {
        String result = UserNameRecognizer.checkForValidUserName("1JohnDoe");
        assertTrue(result.contains("A UserName must start with an alphabetic character"), 
                "A username that does not start with a letter should return an error message.");
    }

    /**
     * Test a username that contains an invalid special character (anything other than underscore).
     */
    @Test
    public void testUserNameRecognizerInvalidSpecialCharacter() {
        String result = UserNameRecognizer.checkForValidUserName("John$Doe1");
        assertTrue(result.contains("A UserName character may only contain the characters A-Z, a-z, 0-9, _"), 
                "A username with an invalid special character should return an error message.");
    }
    
    
    // *******************************
    // Tests for PasswordEvaluator.evaluatePassword
    // *******************************

    /**
     * Test a valid password.
     * A valid password has at least 8 characters, contains at least one uppercase letter,
     * one lowercase letter, one numeric digit, one underscore, and does not contain any blocked substrings.
     */
    @Test
    public void testPasswordEvaluatorValid() {
        String result = PasswordEvaluator.evaluatePassword("Abcdef_1");
        assertEquals("", result, "A valid password should return an empty string.");
    }

    /**
     * Test an empty password.
     */
    @Test
    public void testPasswordEvaluatorEmpty() {
        String result = PasswordEvaluator.evaluatePassword("");
        assertEquals("*** Error *** The password is empty!", result,
                "An empty password should return an error message.");
    }

    /**
     * Test a password missing an uppercase letter.
     */
    @Test
    public void testPasswordEvaluatorMissingUpperCase() {
        String result = PasswordEvaluator.evaluatePassword("abcdef_1");
        assertTrue(result.contains("Upper case"), 
                "A password missing an uppercase letter should mention 'Upper case' in the error message.");
    }

    /**
     * Test a password missing a lowercase letter.
     */
    @Test
    public void testPasswordEvaluatorMissingLowerCase() {
        String result = PasswordEvaluator.evaluatePassword("ABCDEF_1");
        assertTrue(result.contains("Lower case"), 
                "A password missing a lowercase letter should mention 'Lower case' in the error message.");
    }

    /**
     * Test a password missing a numeric digit.
     */
    @Test
    public void testPasswordEvaluatorMissingDigit() {
        String result = PasswordEvaluator.evaluatePassword("Abcdef__");
        assertTrue(result.contains("Numeric digits"), 
                "A password missing a numeric digit should mention 'Numeric digits' in the error message.");
    }

    /**
     * Test a password missing the underscore special character.
     */
    @Test
    public void testPasswordEvaluatorMissingSpecialChar() {
        String result = PasswordEvaluator.evaluatePassword("Abcdef12");
        assertTrue(result.contains("Underscore"), 
                "A password missing an underscore should mention 'Underscore' in the error message.");
    }

    /**
     * Test a password that is too short (less than 8 characters).
     */
    @Test
    public void testPasswordEvaluatorTooShort() {
        String result = PasswordEvaluator.evaluatePassword("Abc_1");
        assertTrue(result.contains("At least 8 characters"), 
                "A password that is too short should mention 'At least 8 characters' in the error message.");
    }

    /**
     * Test a password that contains a blocked substring.
     */
    @Test
    public void testPasswordEvaluatorBlocked() {
        // "password_1" contains the blocked substring "password"
        String result = PasswordEvaluator.evaluatePassword("password_1");
        assertTrue(result.contains("Blocked character"), 
                "A password containing a blocked substring should return an error message about blocked characters.");
    }

    /**
     * Test a password that contains an invalid character (for example, an exclamation mark).
     */
    @Test
    public void testPasswordEvaluatorInvalidCharacter() {
        String result = PasswordEvaluator.evaluatePassword("Abcdef_1!");
        assertEquals("*** Error *** An invalid character has been found!", result,
                "A password with an invalid character should return the appropriate error message.");
    }
}
