package com.example.interviewace.model;

public class OnBoardingItem
{
    private int imageRes;
    private String title;
    private String description;

    // constructor
    public OnBoardingItem(int imageRes, String title, String description) {

        this.imageRes = imageRes;
        this.title = title;
        this.description = description;

    }
    // getter and setter

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
