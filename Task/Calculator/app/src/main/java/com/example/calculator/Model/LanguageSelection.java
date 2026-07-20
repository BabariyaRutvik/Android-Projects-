package com.example.calculator.Model;

public class LanguageSelection
{
    private String name;
    private String languageCode;
    private boolean isSelected;

    // Constructor
    public LanguageSelection(String name, String languageCode, boolean isSelected) {
        this.name = name;
        this.languageCode = languageCode;
        this.isSelected = isSelected;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
