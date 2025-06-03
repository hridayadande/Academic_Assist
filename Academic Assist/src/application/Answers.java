package application;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages a collection of answers in the QandA system.
 * Provides methods for adding, updating, and retrieving answers.
 */
public class Answers {
    private List<Answer> ansList;
    
    /**
     * Initializes a new empty collection of answers.
     */
    public Answers() {
        ansList = new ArrayList<>();
    }
    
    /**
     * Adds a new answer to the collection if it's valid.
     * 
     * @param a The answer to be added.
     */
    public void insertAnswer(Answer a) {
        if (a != null && a.checkValidity()) {
            ansList.add(a);
        }
    }
    
    public void deleteAnswer(int ansID) {
        ansList.removeIf(a -> a.getAnsID() == ansID);
    }
    
    public void modifyAnswer(Answer a) {
        if (a != null) {
            for (int i = 0; i < ansList.size(); i++) {
                if (ansList.get(i).getAnsID() == a.getAnsID()) {
                    ansList.set(i, a);
                    break;
                }
            }
        }
    }
    
    public Answer findAnswerByID(int ansID) {
        return ansList.stream()
                     .filter(a -> a.getAnsID() == ansID)
                     .findFirst()
                     .orElse(null);
    }
    
    public List<Answer> listAllAnswers() {
        return new ArrayList<>(ansList);
    }
    
    public List<Answer> filterAnswers(String keyword) {
        return ansList.stream()
                     .filter(a -> a.getBodyText().toLowerCase().contains(keyword.toLowerCase()))
                     .toList();
    }
    
    public List<Answer> listAnswersForQuestion(int questionID) {
        return ansList.stream()
                     .filter(a -> a.getQuestionID() == questionID)
                     .toList();
    }
}
