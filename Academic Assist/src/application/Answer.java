package application;

import java.util.Date;

/**
 * Represents an answer to a question in the QandA system.
 * Contains information about the answer content, author, and metadata.
 */
public class Answer {
    private int answerID;
    private int questionID;
    private String bodyText;
    private String answeredBy;
    private Date dateCreated;
    
    /**
     * Creates a new Answer with the specified details.
     * 
     * @param ansID The unique identifier for this answer.
     * @param qRefID The ID of the question this answer belongs to.
     * @param bodyText The content of the answer.
     * @param answeredBy The username of the answer's author.
     * @param dateCreated The date when the answer was created.
     */
    public Answer(int ansID, int qRefID, String bodyText, String answeredBy, Date dateCreated) {
        this.answerID = ansID;
        this.questionID = qRefID;
        this.bodyText = bodyText;
        this.answeredBy = answeredBy;
        this.dateCreated = dateCreated;
    }
    
    public int getAnsID() {
        return answerID;
    }
    
    public int getQuestionID() {
        return questionID;
    }
    
    public String getBodyText() {
        return bodyText;
    }
    
    public void setBodyText(String newText) {
        this.bodyText = newText;
    }
    
    public String getAnsweredBy() {
        return answeredBy;
    }
    
    public Date getDateCreated() {
        return dateCreated;
    }
    
    public boolean checkValidity() {
        return bodyText != null && !bodyText.trim().isEmpty() && answeredBy != null;
    }
}
