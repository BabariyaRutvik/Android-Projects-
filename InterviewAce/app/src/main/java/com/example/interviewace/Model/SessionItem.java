package com.example.interviewace.model;

import java.util.List;

public class SessionItem {
    private String roleName;
    private String date;
    private int score;
    private String userId;
    private List<String> skills; // Added skills list

    public SessionItem() {
        // Required for Firestore
    }

    public SessionItem(String roleName, String date, int score, String userId, List<String> skills) {
        this.roleName = roleName;
        this.date = date;
        this.score = score;
        this.userId = userId;
        this.skills = skills;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}
