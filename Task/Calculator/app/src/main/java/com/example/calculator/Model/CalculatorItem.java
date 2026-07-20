package com.example.calculator.Model;

public class CalculatorItem {
    private int nameResId;
    private int iconResId;
    private int textWidth;

    public CalculatorItem(int nameResId, int iconResId, int textWidth) {
        this.nameResId = nameResId;
        this.iconResId = iconResId;
        this.textWidth = textWidth;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getTextWidth() {
        return textWidth;
    }
}
