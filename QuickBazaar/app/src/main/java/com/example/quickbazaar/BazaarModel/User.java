package com.example.quickbazaar.BazaarModel;

public class User
{
    private String userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String createdAt;


    //  parameterized constructor


    public User(String userId, String fullName, String phoneNumber, String email, String createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdAt = createdAt;
    }
    // default constructor


    public User() {
    }


    // getter and setter

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
