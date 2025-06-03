package application;

public class EmailRecognizer{
    // Renamed the method to emailEvaluator to better reflect its purpose
    public static String emailEvaluator(String email) {
        // Check for null or empty email
        if (email == null || email.isEmpty()) {
            return "Error: Email cannot be null or empty.";
        }
        
        // Validate that the email contains only allowed characters:
        // Allowed: alphabets, numbers, '@', and '.'
        if (!email.matches("^[a-zA-Z0-9@.]+$")) {
            return "Error: Email contains invalid characters. Only alphabets, numbers, '@', and '.' are allowed.";
        }
        
        // Validate that the email contains at least one alphabet.
        if (!email.matches(".*[a-zA-Z].*")) {
            return "Error: Email must contain at least one alphabet.";
        }
        
        // Validate that the email contains the '@' character.
        if (!email.contains("@")) {
            return "Error: Email must contain '@'.";
        }
        
        // Validate that the email contains the '.' character.
        if (!email.contains(".")) {
            return "Error: Email must contain '.'.";
        }
        
        // If all validations pass, return a valid message or an empty string.
        return "";
    }
    
    // Example usage:
    public static void main(String[] args) {
        String[] testEmails = {
            "user@example.com",    // Valid
            "userexample.com",     // Missing '@'
            "user@.com",           // Valid, though not a common email format, meets criteria
            "user@ex!ample.com",    // Contains invalid character '!'
            "12345@6789.0",        // Valid if considered as meeting criteria
            "@example.com",        // Missing alphabet? (There is no alphabet in '@')
            "userexamplecom"       // Missing '@' and '.'
        };
        
        for (String email : testEmails) {
            System.out.println("Testing: " + email + " -> " + emailEvaluator(email));
        }
    }
}
