package com.example.quicknotes.Model;

public class CategoryModel {
    private String categoryKey;
    private String categoryName;
    private int backgroundColor;
    private int lightColor;
    private boolean isSelected;

    public CategoryModel(String categoryKey, String categoryName, int backgroundColor, int lightColor, boolean isSelected) {
        this.categoryKey = categoryKey;
        this.categoryName = categoryName;
        this.backgroundColor = backgroundColor;
        this.lightColor = lightColor;
        this.isSelected = isSelected;
    }

    public CategoryModel(String categoryName, int backgroundColor, int lightColor, boolean isSelected) {
        this.categoryKey = categoryName;
        this.categoryName = categoryName;
        this.backgroundColor = backgroundColor;
        this.lightColor = lightColor;
        this.isSelected = isSelected;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getLightColor() {
        return lightColor;
    }

    public void setLightColor(int lightColor) {
        this.lightColor = lightColor;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
