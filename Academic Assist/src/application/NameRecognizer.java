package application;

public class NameRecognizer {
    public static String nameEvaluator(String firstname, String lastname) {
        // Check for null values (optional but recommended)
        if (firstname == null || lastname == null) {
            return "Error: First name and Last name cannot be null.";
        }
        
        // Check length constraints
        if (firstname.length() > 30) {
            return "Error: First name exceeds 30 characters.";
        }
        if (lastname.length() > 30) {
            return "Error: Last name exceeds 30 characters.";
        }
        
        // Define a regular expression for allowed characters: only alphabets and spaces.
        String validPattern = "^[a-zA-Z ]+$";
        
        // Validate that the first name matches the pattern.
        if (!firstname.matches(validPattern)) {
            return "Error: First name contains invalid characters. Only alphabets and spaces are allowed.";
        }
        
        // Validate that the last name matches the pattern.
        if (!lastname.matches(validPattern)) {
            return "Error: Last name contains invalid characters. Only alphabets and spaces are allowed.";
        }
        
        // If all validations pass, you can return a success message or simply an empty string.
        return "";
        
        
    }
}
