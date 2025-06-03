package databasePart1;

import java.sql.*;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

import application.Request;
import application.ReviewerRequest;
import application.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

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
            // If you want to reset database just uncomment the line below
           //statement.execute("DROP ALL OBJECTS");

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
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "role VARCHAR(255), "
                + "firstName VARCHAR(255), "
                + "lastName VARCHAR(255), "
                + "email VARCHAR(255))";
        statement.execute(userTable);
        
        // Create the invitation codes table
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "role VARCHAR(255), "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationCodesTable);

        // Create the reviewer requests table
        String reviewerRequestsTable = "CREATE TABLE IF NOT EXISTS ReviewerRequests ("
                + "requestID VARCHAR(36) PRIMARY KEY, "
                + "studentName VARCHAR(255), "
                + "instructorUsername VARCHAR(255), "
                + "requestMessage TEXT, "
                + "requestDate TIMESTAMP, "
                + "status VARCHAR(20) DEFAULT 'PENDING')";
        statement.execute(reviewerRequestsTable);

        // Create the admin access requests table
        String adminAccessRequestsTable = "CREATE TABLE IF NOT EXISTS admin_access_requests ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(255), "
                + "reason TEXT, "
                + "request_date TIMESTAMP, "
                + "status VARCHAR(20) DEFAULT 'pending', "
                + "description TEXT, "
                + "reopened_from INT)";
        statement.execute(adminAccessRequestsTable);
        
        // Check if description column exists in admin_access_requests table and add it if not
        try {
            // Try to execute a simple query that selects the description column
            statement.executeQuery("SELECT description FROM admin_access_requests LIMIT 1");
        } catch (SQLException e) {
            // Column doesn't exist, add it
            if (e.getMessage().contains("Column \"DESCRIPTION\" not found") || 
                e.getMessage().contains("not found: DESCRIPTION")) {
                System.out.println("Adding description column to admin_access_requests table");
                try {
                    statement.execute("ALTER TABLE admin_access_requests ADD COLUMN description TEXT");
                    
                    // Copy reason to description for existing records
                    statement.execute("UPDATE admin_access_requests SET description = reason WHERE description IS NULL");
                } catch (SQLException ex) {
                    System.err.println("Failed to add description column: " + ex.getMessage());
                }
            }
        }
    }


    /**
     * Checks if the database is empty.
     */
    public boolean isDatabaseEmpty() throws SQLException {
        // Ensure connection is open before querying
        ensureConnected();
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    /**
     * Registers a new user in the database.
     */
    public void register(User user) throws SQLException {
        // Ensure connection is open before executing any operation
        ensureConnected();
        String insertUser = "INSERT INTO cse360users (userName, password, role, firstName, lastName, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getfirstName()); // or getFirstName() if renamed
            pstmt.setString(5, user.getlastName());  // or getLastName() if renamed
            pstmt.setString(6, user.getemail());     // or getEmail() if renamed
            pstmt.executeUpdate();
        }
    }


    /**
     * Validates a user's login credentials.
     */
    public boolean login(User user) throws SQLException {
        // Ensure connection is open before executing any operation
        ensureConnected();
        
        // Modified query to only check username and password
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Get the stored role from the database
                    String storedRole = rs.getString("role");
                    String requestedRole = user.getRole();
                    
                    // Case insensitive check for empty role or matching role
                    if (requestedRole.isEmpty() || 
                        storedRole.equalsIgnoreCase(requestedRole) || 
                        storedRole.toLowerCase().contains(requestedRole.toLowerCase())) {
                        return true;
                    }
                }
                return false;
            }
        }
    }
    
    /**
     * Checks if a user already exists in the database based on their userName.
     */
    public boolean doesUserExist(String userName) {
        try {
            // Ensure connection is open before executing any operation
            ensureConnected();
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // If the count is greater than 0, the user exists
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // If an error occurs, assume user doesn't exist
    }
    
    /**
     * Retrieves the role of a user from the database using their userName.
     */
    public String getUserRole(String userName) {
        try {
            // Ensure connection is open before executing any operation
            ensureConnected();
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
        
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role"); // Return the role if user exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // If no user exists or an error occurs
    }
    
    /**
     * Generates a new invitation code with associated role and inserts it into the database.
     */
    public String generateInvitationCodeWithRole(String role) {
        try {
            ensureConnected();
            
            String code = UUID.randomUUID().toString().substring(0, 4);
            String query = "INSERT INTO InvitationCodes (code, role) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, code);
                pstmt.setString(2, role);
                pstmt.executeUpdate();
                return code;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Validates an invitation code and returns the associated role if valid.
     */
    public String validateInvitationCodeAndGetRole(String code) {
        try {
            ensureConnected();
            
            String query = "SELECT role FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, code);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role");
                    markInvitationCodeAsUsed(code);
                    return role;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Marks the invitation code as used in the database.
     */
    private void markInvitationCodeAsUsed(String code) {
        try {
            // Ensure connection is open before executing any operation
            ensureConnected();
        } catch (SQLException e1) {
            e1.printStackTrace();
            return;
        }
        
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves all users from the database.
     * @return An ObservableList of User objects.
     * @throws SQLException if a database access error occurs.
     */
    public ObservableList<User> getAllUsers() throws SQLException {
        // Ensure we are connected to the database.
        ensureConnected();
        ObservableList<User> userList = FXCollections.observableArrayList();
        // Retrieve userName, role, firstName, lastName, and email.
        String query = "SELECT userName, role, firstName, lastName, email FROM cse360users";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String userName = rs.getString("userName");
                String role = rs.getString("role");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String email = rs.getString("email");
                // Create a User object. We use an empty string for the password.
                User user = new User(userName, "", role, firstName, lastName, email);
                userList.add(user);
            }
        }
        return userList;
    }

    
    /**
     * Deletes a user from the database based on their userName.
     * <p>
     * Note: This method will not delete an admin user.
     * </p>
     * @param userName The username of the user to be deleted.
     * @return true if a user was deleted, false otherwise.
     */
    public boolean deleteUser(String userName) {
        try {
            ensureConnected();
            // Prevent deletion if the user is an admin
            String role = getUserRole(userName);
            if (role != null && role.equalsIgnoreCase("admin")) {
                System.out.println("Cannot delete an admin user.");
                return false;
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        
        String query = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Updates the users password.
     */
    public void updatePassword(String username, String newPassword) throws SQLException {
        ensureConnected();
        String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }
    /**
     * Updates the users password.
     */
    public boolean isOTPValid(String username, String otp) throws SQLException {
        ensureConnected();
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, otp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;  // Returns true if OTP matches the stored password
            }
        }
        return false; // OTP is invalid
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

    public String getUserFirstName(String userName) throws SQLException {
        ensureConnected();
        String query = "SELECT firstName FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("firstName");
            }
        }
        return "";
    }

    public String getUserLastName(String userName) throws SQLException {
        ensureConnected();
        String query = "SELECT lastName FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("lastName");
            }
        }
        return "";
    }

    public String getUserEmail(String userName) throws SQLException {
        ensureConnected();
        String query = "SELECT email FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        }
        return "";
    }
    //creates request array
    private static List<ReviewerRequest> reviewerRequestsDatabase = new ArrayList<>();
	 public void insertReviewerRequest(ReviewerRequest newRequest) throws SQLException {
        ensureConnected();
        String query = "INSERT INTO ReviewerRequests (requestID, studentName, instructorUsername, requestMessage, requestDate) "
                     + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newRequest.getRequestID());
            pstmt.setString(2, newRequest.getStudentName());
            pstmt.setString(3, newRequest.getInstructorUsername());
            pstmt.setString(4, newRequest.getRequestMessage());
            pstmt.setTimestamp(5, Timestamp.valueOf(newRequest.getRequestDate()));
            pstmt.executeUpdate();
        }
    }
	    // Fetch all reviewer requests
	    public List<ReviewerRequest> getAllReviewerRequests() throws SQLException {
	        ensureConnected();
	        List<ReviewerRequest> requests = new ArrayList<>();
	        String query = "SELECT * FROM ReviewerRequests ORDER BY requestDate DESC";
	        try (PreparedStatement pstmt = connection.prepareStatement(query);
	             ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                ReviewerRequest request = new ReviewerRequest(
	                    rs.getString("studentName"),
	                    rs.getString("instructorUsername"),
	                    rs.getString("requestMessage"),
	                    rs.getTimestamp("requestDate").toLocalDateTime()
	                );
	                request.setRequestID(rs.getString("requestID"));
	                requests.add(request);
	            }
	        }
	        return requests;
	    }
	    public boolean doesReviewerRequestExist(String studentName) throws SQLException {
	        ensureConnected();
	        String query = "SELECT COUNT(*) FROM ReviewerRequests WHERE studentName = ? AND status = 'PENDING'";
	        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	            pstmt.setString(1, studentName);
	            ResultSet rs = pstmt.executeQuery();
	            if (rs.next()) {
	                return rs.getInt(1) > 0;
	            }
	        }
	        return false;
	    }

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

    /**
     * Gets all instructors from the database.
     * @return A list of instructor information arrays [username, firstName, lastName]
     */
    public List<String[]> getAllInstructors() throws SQLException {
        ensureConnected();
        List<String[]> instructors = new ArrayList<>();
        String query = "SELECT userName, firstName, lastName FROM cse360users WHERE role LIKE '%Instructor%'";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                instructors.add(new String[]{
                    rs.getString("userName"),
                    rs.getString("firstName"),
                    rs.getString("lastName")
                });
            }
        }
        return instructors;
    }

    /**
     * Gets all staff and instructors from the database.
     * @return A list of staff and instructor information arrays [username, firstName, lastName]
     */
    public List<String[]> getAllStaffAndInstructors() throws SQLException {
        ensureConnected();
        List<String[]> staffAndInstructors = new ArrayList<>();
        String query = "SELECT userName, firstName, lastName FROM cse360users WHERE role LIKE '%Staff%' OR role LIKE '%Instructor%'";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                staffAndInstructors.add(new String[]{
                    rs.getString("userName"),
                    rs.getString("firstName"),
                    rs.getString("lastName")
                });
            }
        }
        return staffAndInstructors;
    }

    public void updateReviewerRequestStatus(String requestID, String status) throws SQLException {
        ensureConnected();
        String query = "UPDATE ReviewerRequests SET status = ? WHERE requestID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setString(2, requestID);
            pstmt.executeUpdate();
        }
    }

    public List<ReviewerRequest> getReviewerRequestsForInstructor(String instructorUsername) throws SQLException {
        ensureConnected();
        List<ReviewerRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM ReviewerRequests WHERE instructorUsername = ? AND status = 'PENDING' ORDER BY requestDate DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, instructorUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ReviewerRequest request = new ReviewerRequest(
                    rs.getString("studentName"),
                    rs.getString("instructorUsername"),
                    rs.getString("requestMessage"),
                    rs.getTimestamp("requestDate").toLocalDateTime()
                );
                request.setRequestID(rs.getString("requestID"));
                requests.add(request);
            }
        }
        return requests;
    }

    public void updateUserRole(String userName, String newRole) throws SQLException {
        ensureConnected();
        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newRole);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Gets a list of all users that have the Restricted flag in their role.
     * 
     * @return A list of restricted users, where each element is an array of 
     *         [username, firstName, lastName, role]
     * @throws SQLException if a database error occurs
     */
    public List<String[]> getRestrictedUsers() throws SQLException {
        ensureConnected();
        List<String[]> restrictedUsers = new ArrayList<>();
        
        String query = "SELECT userName, firstName, lastName, role FROM cse360users " +
                      "WHERE role LIKE '%Restricted%'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String username = rs.getString("userName");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String role = rs.getString("role");
                
                restrictedUsers.add(new String[]{username, firstName, lastName, role});
            }
        }
        
        return restrictedUsers;
    }

    /**
     * Submits an admin access request
     */
    public void submitAdminAccessRequest(String username, String reason) throws SQLException {
        ensureConnected();
        
        try {
            // Try with description column
            String query = "INSERT INTO admin_access_requests (username, reason, request_date, status, description) VALUES (?, ?, CURRENT_TIMESTAMP, 'pending', ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, reason);
                pstmt.setString(3, reason); // Set description equal to reason initially
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // If error involves description column, try without it
            if (e.getMessage().contains("DESCRIPTION") || e.getMessage().contains("description")) {
                System.out.println("Falling back to insert without description column");
                String query = "INSERT INTO admin_access_requests (username, reason, request_date, status) VALUES (?, ?, CURRENT_TIMESTAMP, 'pending')";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, reason);
                    pstmt.executeUpdate();
                }
                
                // Try to add the description column if it doesn't exist
                try {
                    statement.execute("ALTER TABLE admin_access_requests ADD COLUMN description TEXT");
                    System.out.println("Added description column to admin_access_requests table");
                    
                    // Update the record we just inserted
                    String updateQuery = "UPDATE admin_access_requests SET description = ? WHERE username = ? AND status = 'pending'";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, reason);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    // Ignore if column already exists or can't be added
                    System.err.println("Failed to add or update description column: " + ex.getMessage());
                }
            } else {
                // If it's some other error, throw it
                throw e;
            }
        }
    }

    /**
     * Gets all pending admin access requests
     */
    public ObservableList<Request> getAdminAccessRequests() throws SQLException {
        ensureConnected();
        ObservableList<Request> requests = FXCollections.observableArrayList();
        
        try {
            // Try with ID, description and reopened_from columns
            String query = "SELECT id, username, reason, request_date, description, reopened_from FROM admin_access_requests WHERE status = 'pending' ORDER BY request_date DESC";
            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Request request = new Request(
                        rs.getString("username"),
                        rs.getString("reason"),
                        rs.getString("request_date")
                    );
                    request.setId(rs.getInt("id"));
                    
                    // Get description or use reason if description is null
                    String description = rs.getString("description");
                    if (description == null) {
                        description = rs.getString("reason");
                    }
                    request.setDescription(description);
                    
                    // Set reopened_from if it exists
                    int reopenedFrom = rs.getInt("reopened_from");
                    if (!rs.wasNull()) {
                        request.setReopenedFromId(reopenedFrom);
                    }
                    
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            // Handle the case where reopened_from column doesn't exist
            if (e.getMessage().contains("REOPENED_FROM") || e.getMessage().contains("reopened_from")) {
                // Try to add the reopened_from column
                try {
                    statement.execute("ALTER TABLE admin_access_requests ADD COLUMN reopened_from INT");
                    System.out.println("Added reopened_from column to admin_access_requests table");
                } catch (SQLException ex) {
                    // Ignore if column already exists or can't be added
                    System.err.println("Failed to add reopened_from column: " + ex.getMessage());
                }
                
                // Retry the query without reopened_from
                String query = "SELECT id, username, reason, request_date, description FROM admin_access_requests WHERE status = 'pending' ORDER BY request_date DESC";
                try (PreparedStatement pstmt = connection.prepareStatement(query);
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Request request = new Request(
                            rs.getString("username"),
                            rs.getString("reason"),
                            rs.getString("request_date")
                        );
                        request.setId(rs.getInt("id"));
                        
                        // Get description or use reason if description is null
                        String description = rs.getString("description");
                        if (description == null) {
                            description = rs.getString("reason");
                        }
                        request.setDescription(description);
                        
                        requests.add(request);
                    }
                }
            } else {
                // If it's some other error, throw it
                throw e;
            }
        }
        
        return requests;
    }

    /**
     * Approves an admin access request
     */
    public void approveAdminAccessRequest(String username) throws SQLException {
        ensureConnected();
        String query = "UPDATE admin_access_requests SET status = 'approved' WHERE username = ? AND status = 'pending'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
        
        // Add Admin role to user (capitalized for consistency with other roles)
        query = "UPDATE cse360users SET role = CONCAT(role, ',Admin') WHERE userName = ? AND role NOT LIKE '%Admin%' AND role NOT LIKE '%admin%'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    /**
     * Denies an admin access request
     */
    public void denyAdminAccessRequest(String username) throws SQLException {
        ensureConnected();
        String query = "UPDATE admin_access_requests SET status = 'denied' WHERE username = ? AND status = 'pending'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Checks if a user has already requested admin access and the request is pending
     * 
     * @param username The username to check
     * @return true if the user has a pending request, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean hasUserRequestedAdminAccess(String username) throws SQLException {
        ensureConnected();
        String query = "SELECT COUNT(*) FROM admin_access_requests WHERE username = ? AND status = 'pending'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Checks if a user's admin access request was approved
     * 
     * @param username The username to check
     * @return true if the user has an approved request, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean isUserRequestApproved(String username) throws SQLException {
        ensureConnected();
        String query = "SELECT COUNT(*) FROM admin_access_requests WHERE username = ? AND status = 'approved'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    // Fetch a list of admin access requests that have been marked as 'closed'
    public ObservableList<Request> getClosedAdminRequests() throws SQLException {
        ensureConnected();
        ObservableList<Request> closedRequests = FXCollections.observableArrayList();
        
        // First try with description column
        try {
            String query = "SELECT id, username, reason, request_date, description FROM admin_access_requests WHERE status = 'closed'";
            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Request request = new Request(
                        rs.getString("username"),
                        rs.getString("reason"),
                        rs.getString("request_date")
                    );
                    request.setId(rs.getInt("id"));
                    
                    // Get description or use reason if description is null
                    String description = rs.getString("description");
                    if (description == null) {
                        description = rs.getString("reason");
                    }
                    request.setDescription(description);
                    
                    closedRequests.add(request);
                }
            }
        } catch (SQLException e) {
            // If description column doesn't exist, try without it
            if (e.getMessage().contains("DESCRIPTION") || e.getMessage().contains("description")) {
                System.out.println("Falling back to query without description column");
                String query = "SELECT id, username, reason, request_date FROM admin_access_requests WHERE status = 'closed'";
                try (PreparedStatement pstmt = connection.prepareStatement(query);
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Request request = new Request(
                            rs.getString("username"),
                            rs.getString("reason"),
                            rs.getString("request_date")
                        );
                        request.setId(rs.getInt("id"));
                        
                        // Use reason as description
                        request.setDescription(rs.getString("reason"));
                        
                        closedRequests.add(request);
                    }
                }
            } else {
                // If it's some other error, throw it
                throw e;
            }
        }
        
        return closedRequests;
    }

    /**
     * Adds a record to closed admin requests when admin privileges are removed
     * 
     * @param username The username whose admin privileges were removed
     * @param reason The reason for removing admin privileges
     * @param date The date when admin privileges were removed
     * @throws SQLException if a database error occurs
     */
    public void addClosedAdminRequest(String username, String reason, String date) throws SQLException {
        ensureConnected();
        
        try {
            // Try with description column
            String query = "INSERT INTO admin_access_requests (username, reason, request_date, status, description) VALUES (?, ?, ?, 'closed', ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, reason);
                pstmt.setString(3, date);
                pstmt.setString(4, reason); // Initially, description is the same as reason
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // If error involves description column, try without it
            if (e.getMessage().contains("DESCRIPTION") || e.getMessage().contains("description")) {
                System.out.println("Falling back to insert without description column");
                String query = "INSERT INTO admin_access_requests (username, reason, request_date, status) VALUES (?, ?, ?, 'closed')";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, reason);
                    pstmt.setString(3, date);
                    pstmt.executeUpdate();
                }
                
                // Try to add the description column if it doesn't exist
                try {
                    statement.execute("ALTER TABLE admin_access_requests ADD COLUMN description TEXT");
                    System.out.println("Added description column to admin_access_requests table");
                    
                    // Update the record we just inserted
                    String updateQuery = "UPDATE admin_access_requests SET description = ? WHERE username = ? AND request_date = ? AND status = 'closed'";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, reason);
                        updateStmt.setString(2, username);
                        updateStmt.setString(3, date);
                        updateStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    // Ignore if column already exists or can't be added
                    System.err.println("Failed to add or update description column: " + ex.getMessage());
                }
            } else {
                // If it's some other error, throw it
                throw e;
            }
        }
    }
    
    /**
     * Reopens a closed admin request
     * @param requestId The ID of the request to reopen
     * @throws SQLException if a database error occurs
     */
    public void reopenAdminRequest(int requestId) throws SQLException {
        ensureConnected();

        // First get the closed request details before we change its status
        String getRequest = "SELECT id, username, reason, description FROM admin_access_requests WHERE id = ? AND status = 'closed'";
        int closedRequestId = -1;
        String username = "";
        String reason = "";
        String description = "";
        
        try (PreparedStatement pstmt = connection.prepareStatement(getRequest)) {
            pstmt.setInt(1, requestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    closedRequestId = rs.getInt("id");
                    username = rs.getString("username");
                    reason = rs.getString("reason");
                    description = rs.getString("description");
                    if (description == null) {
                        description = reason;
                    }
                }
            }
        }
        
        if (closedRequestId == -1) {
            throw new SQLException("Could not find closed request with ID: " + requestId);
        }
        
        // Insert a new pending request with a reference to the closed request
        String insertQuery = "INSERT INTO admin_access_requests (username, reason, request_date, status, description, reopened_from) VALUES (?, ?, ?, 'pending', ?, ?)";
        String currentDate = java.time.LocalDate.now().toString();
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, username);
            // Add "Reopened: " prefix to the reason
            pstmt.setString(2, "Reopened: " + reason);
            pstmt.setString(3, currentDate);
            // Add "Reopened: " prefix to the description
            pstmt.setString(4, "Reopened: " + description);
            pstmt.setInt(5, closedRequestId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Updates the description of an admin request
     * @param requestId The ID of the request to update
     * @param description The new description
     * @throws SQLException if a database error occurs
     */
    public void updateAdminRequestDescription(int requestId, String description) throws SQLException {
        ensureConnected();
        String query = "UPDATE admin_access_requests SET description = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, description);
            pstmt.setInt(2, requestId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates existing lowercase 'admin' roles to capitalized 'Admin' for consistency
     * This should be called during application startup to ensure all roles are properly capitalized
     * 
     * @throws SQLException if a database error occurs
     */
    public void normalizeAdminRoles() throws SQLException {
        ensureConnected();
        
        // First, back up user roles to prevent data loss
        String backupQuery = "CREATE TABLE IF NOT EXISTS role_backup AS SELECT userName, role FROM cse360users";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(backupQuery);
        }
        
        // Update comma-separated cases first: ",admin," to ",Admin,"
        String query = "UPDATE cse360users SET role = REPLACE(role, ',admin,', ',Admin,') WHERE role LIKE '%,admin,%'";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
        
        // Handle cases where 'admin' is at the end: ",admin" to ",Admin"
        query = "UPDATE cse360users SET role = REPLACE(role, ',admin', ',Admin') WHERE role LIKE '%,admin'";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
        
        // Handle cases where 'admin' is at the beginning: "admin," to "Admin,"
        query = "UPDATE cse360users SET role = REPLACE(role, 'admin,', 'Admin,') WHERE role LIKE 'admin,%'";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
        
        // Handle case where 'admin' is the only role
        query = "UPDATE cse360users SET role = 'Admin' WHERE role = 'admin'";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
        
        // Verify if any users still have lowercase 'admin' in their roles
        query = "SELECT COUNT(*) FROM cse360users WHERE role LIKE '%admin%'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Warning: Some users still have lowercase 'admin' in their roles after normalization.");
            }
        }
    }

    /**
     * Gets a specific admin access request by ID
     * @param requestId The ID of the request to get
     * @return The request object, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Request getAdminRequestById(int requestId) throws SQLException {
        ensureConnected();
        String query = "SELECT id, username, reason, request_date, description, status FROM admin_access_requests WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, requestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Request request = new Request(
                        rs.getString("username"),
                        rs.getString("reason"),
                        rs.getString("request_date")
                    );
                    request.setId(rs.getInt("id"));
                    
                    // Get description or use reason if description is null
                    String description = rs.getString("description");
                    if (description == null) {
                        description = rs.getString("reason");
                    }
                    request.setDescription(description);
                    
                    return request;
                }
            }
        }
        
        return null;
    }
}