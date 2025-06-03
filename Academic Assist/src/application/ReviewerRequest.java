package application;
import java.time.LocalDateTime;

public class ReviewerRequest {

    private String requestID;
    private String studentName;
    private String instructorUsername;
    private String requestMessage;
    private LocalDateTime requestDate;

    //preps for the request to be made 
    public ReviewerRequest(String studentName, String instructorUsername, String requestMessage, LocalDateTime requestDate) {
        this.studentName = studentName;
        this.instructorUsername = instructorUsername;
        this.requestMessage = requestMessage;
        this.requestDate = requestDate;
    }

   
    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getInstructorUsername() {
        return instructorUsername;
    }

    public void setInstructorUsername(String instructorUsername) {
        this.instructorUsername = instructorUsername;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
}
