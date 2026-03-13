package com.example.medminder.model;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private int age;

    // No-argument constructor required for Firebase
    public User() {
    }

    public User(String uid, String fullName, String email, int age) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.age = age;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
