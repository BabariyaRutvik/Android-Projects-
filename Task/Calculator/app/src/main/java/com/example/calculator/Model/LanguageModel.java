package com.example.calculator.Model;

public class LanguageModel {
    private String name;
    private String code;
    private boolean isSelected;

    public LanguageModel(String name, String code, boolean isSelected) {
        this.name = name;
        this.code = code;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
