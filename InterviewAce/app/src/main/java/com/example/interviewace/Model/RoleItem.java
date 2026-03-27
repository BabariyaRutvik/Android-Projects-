package com.example.interviewace.model;

public class RoleItem {
    private String roleName;
    private String questionCount;
    private String iconName;
    private String bgColor;

    public RoleItem() {
        // Required for Firestore
    }

    public RoleItem(String roleName, String questionCount, String iconName, String bgColor) {
        this.roleName = roleName;
        this.questionCount = questionCount;
        this.iconName = iconName;
        this.bgColor = bgColor;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(String questionCount) {
        this.questionCount = questionCount;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
}
