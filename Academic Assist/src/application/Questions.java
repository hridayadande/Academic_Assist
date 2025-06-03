package application;

import java.util.List;
import java.util.ArrayList;

public class Questions {
    private List<Question> qList;
    
    public Questions() {
        qList = new ArrayList<>();
    }
    
    public void insertQuestion(Question q) {
        if (q != null && q.checkValidity()) {
            qList.add(q);
        }
    }
    
    public void deleteQuestion(int qID) {
        qList.removeIf(q -> q.getQuestionID() == qID);
    }
    
    public void modifyQuestion(Question q) {
        if (q != null) {
            for (int i = 0; i < qList.size(); i++) {
                if (qList.get(i).getQuestionID() == q.getQuestionID()) {
                    qList.set(i, q);
                    break;
                }
            }
        }
    }
    
    public Question findQuestionByID(int qID) {
        return qList.stream()
                   .filter(q -> q.getQuestionID() == qID)
                   .findFirst()
                   .orElse(null);
    }
    
    public List<Question> listAllQuestions() {
        return new ArrayList<>(qList);
    }
    
    public List<Question> searchQuestions(String keyword) {
        return qList.stream()
                   .filter(q -> q.getBodyText().toLowerCase().contains(keyword.toLowerCase()))
                   .toList();
    }
    
    public List<Question> getQList() {
        return qList;
    }
}
