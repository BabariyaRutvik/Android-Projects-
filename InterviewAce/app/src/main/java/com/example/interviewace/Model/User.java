package com.example.interviewace.model;

public class User {

    private String userId;
    private String fullName;
    private String email;
    private String college;
    private String targetRole;
    private String profilePic;
    private String graduationYear;

    private int totalSessions;
    private int currentStreak;
    private float avgScore;
    private int totalCertificates;

    private long joinedDate;
    private long lastActiveDate;

    public User() {} // Required for Firestore

    public User(String userId, String fullName, String email, String college, String targetRole) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.college = college;
        this.targetRole = targetRole;
        this.profilePic = "";
        this.graduationYear = "";
        this.totalSessions = 0;
        this.currentStreak = 0;
        this.avgScore = 0;
        this.totalCertificates = 0;
        this.joinedDate = System.currentTimeMillis();
        this.lastActiveDate = System.currentTimeMillis();
    }

    public User(String userId, String fullName, String email, String profilePic) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.college = "";
        this.targetRole = "";
        this.profilePic = profilePic;
        this.graduationYear = "";
        this.totalSessions = 0;
        this.currentStreak = 0;
        this.avgScore = 0;
        this.totalCertificates = 0;
        this.joinedDate = System.currentTimeMillis();
        this.lastActiveDate = System.currentTimeMillis();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getGraduationYear() { return graduationYear; }
    public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public float getAvgScore() { return avgScore; }
    public void setAvgScore(float avgScore) { this.avgScore = avgScore; }

    public int getTotalCertificates() { return totalCertificates; }
    public void setTotalCertificates(int totalCertificates) { this.totalCertificates = totalCertificates; }

    public long getJoinedDate() { return joinedDate; }
    public void setJoinedDate(long joinedDate) { this.joinedDate = joinedDate; }

    public long getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(long lastActiveDate) { this.lastActiveDate = lastActiveDate; }
}
