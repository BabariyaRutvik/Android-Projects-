package com.example.quicknotes.Model;

public class WidgetModel {
    private String size;
    private int previewImage;
    private boolean isSelected;

    public WidgetModel(String size, int previewImage) {
        this.size = size;
        this.previewImage = previewImage;
        this.isSelected = false;
    }

    public String getSize() { return size; }
    public int getPreviewImage() { return previewImage; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}