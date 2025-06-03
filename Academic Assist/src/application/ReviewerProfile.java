package application;

import java.util.Date;

public class ReviewerProfile {
    private String userName;
    private String experience;
    private String background;
    private Date lastUpdated;
    private int totalReviews;
    private double averageRating;

    public ReviewerProfile(String userName, String experience, String background) {
        this.userName = userName;
        this.experience = experience;
        this.background = background;
        this.lastUpdated = new Date();
    }

    // Getters and setters
    public String getUserName() {
        return userName;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
        this.lastUpdated = new Date();
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
        this.lastUpdated = new Date();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
} 