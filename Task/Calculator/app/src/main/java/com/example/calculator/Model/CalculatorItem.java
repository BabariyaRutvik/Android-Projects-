package com.example.calculator.Model;

public class CalculatorItem {
    private String name;
    private int iconResId;
    private int textWidth;

    public CalculatorItem(String name, int iconResId, int textWidth) {
        this.name = name;
        this.iconResId = iconResId;
        this.textWidth = textWidth;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getTextWidth() {
        return textWidth;
    }
}
