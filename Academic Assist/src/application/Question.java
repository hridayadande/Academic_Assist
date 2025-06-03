package application;
import java.util.Date;  

/**
 * Represents a question in the QandA system.
 * Contains information about the question content, author, status, and metadata.
 */
public class Question {
    private int questionID;
    private String bodyText;
    private String postedBy;
    private Date dateCreated;
    private boolean resolvedStatus;
    private int acceptedAnsID;
    private int newMessagesCount;
    
    /**
     * Creates a new Question with the specified details.
     * 
     * @param qID The unique identifier for this question.
     * @param bodyText The content of the question.
     * @param postedBy The username of the question's author.
     * @param dateCreated The date when the question was created.
     */
    public Question(int qID, String bodyText, String postedBy, Date dateCreated) {
        this.questionID = qID;
        this.bodyText = bodyText;
        this.postedBy = postedBy;
        this.dateCreated = dateCreated;
        this.resolvedStatus = false;
        this.acceptedAnsID = -1;
        this.newMessagesCount = 0;
    }
    
    public int getQuestionID() {
        return questionID;
    }
    
    public void setQuestionID(int id) {
        this.questionID = id;
    }
    
    public String getBodyText() {
        return bodyText;
    }
    
    public void setBodyText(String newText) {
        this.bodyText = newText;
    }
    
    public void setPostedBy(String newPostedBy) {
        this.postedBy = newPostedBy;
    }
    
    public String getPostedBy() {
        return postedBy;
    }
    
    public Date getDateCreated() {
        return dateCreated;
    }
    
    public boolean isResolved() {
        return resolvedStatus;
    }
    
    public void setResolved(boolean status) {
        this.resolvedStatus = status;
    }
    
    public int getAcceptedAnsID() {
        return acceptedAnsID;
    }
    
    public void setAcceptedAnsID(int ansID) {
        this.acceptedAnsID = ansID;
    }
    
    public int getNewMessagesCount() {
        return newMessagesCount;
    }
    
    public void setNewMessagesCount(int count) {
        this.newMessagesCount = count;
    }
    
    public boolean checkValidity() {
        return bodyText != null && !bodyText.trim().isEmpty() && postedBy != null;
    }
    
    public void setDateCreated(Date d) {
        this.dateCreated = d;
    }
}
