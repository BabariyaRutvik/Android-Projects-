package com.example.interviewace.model;

public class AnswerModel {
    private String question;
    private String answer;
    private String role;
    private String difficulty;
    private long timestamp;
    private FeedbackResponse feedback;
    private String userId;

    public AnswerModel() {
        // Required for Firestore
    }

    public AnswerModel(String question, String answer, String role, String difficulty, long timestamp, String userId) {
        this.question = question;
        this.answer = answer;
        this.role = role;
        this.difficulty = difficulty;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public FeedbackResponse getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackResponse feedback) {
        this.feedback = feedback;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
