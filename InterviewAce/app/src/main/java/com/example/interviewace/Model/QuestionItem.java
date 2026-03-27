package com.example.interviewace.model;

import java.util.List;

public class QuestionItem {
    private String difficulty;
    private String questionText;
    private String role;
    private List<String> tags;

    public QuestionItem() {
        // Required for Firestore
    }

    public QuestionItem(String difficulty, String questionText, String role, List<String> tags) {
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.role = role;
        this.tags = tags;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
