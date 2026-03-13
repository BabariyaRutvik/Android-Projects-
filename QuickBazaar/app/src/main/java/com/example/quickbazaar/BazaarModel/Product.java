package com.example.quickbazaar.BazaarModel;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String id;
    private String name;
    private String brand;
    private String categoryId;
    private String subcategoryId;
    private String description;
    private double price;
    private double discountPrice;
    private List<String> images;
    private boolean isAvailable;
    private boolean isPopular;
    private double rating;
    private int stock;
    private String unit;

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Support for typo "name " in Firestore
    @PropertyName("name ")
    public String getNameWithSpace() { return name; }
    @PropertyName("name ")
    public void setNameWithSpace(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    // Support for field "subcategoryId"
    public String getSubcategoryId() { return subcategoryId; }
    public void setSubcategoryId(String subcategoryId) { this.subcategoryId = subcategoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Support for typo "prrice"
    @PropertyName("prrice")
    @Exclude
    public double getPrrice() { return price; }
    @PropertyName("prrice")
    public void setPrrice(Object price) { this.price = convertToDouble(price); }

    @Exclude
    public double getPrice() { return price; }
    @PropertyName("price")
    public void setPrice(Object price) { this.price = convertToDouble(price); }
    @PropertyName("price")
    public Object getPriceForFirestore() { return price; }

    // Support for typo "disscountPrice"
    @PropertyName("disscountPrice")
    @Exclude
    public double getDisscountPrice() { return discountPrice; }
    @PropertyName("disscountPrice")
    public void setDisscountPrice(Object discountPrice) { this.discountPrice = convertToDouble(discountPrice); }

    @Exclude
    public double getDiscountPrice() { return discountPrice; }
    @PropertyName("discountPrice")
    public void setDiscountPrice(Object discountPrice) { this.discountPrice = convertToDouble(discountPrice); }
    @PropertyName("discountPrice")
    public Object getDiscountPriceForFirestore() { return discountPrice; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    // Support for typo "isAvaailable"
    @PropertyName("isAvaailable")
    public boolean isAvaailable() { return isAvailable; }
    @PropertyName("isAvaailable")
    public void setAvaailable(boolean available) { isAvailable = available; }

    @PropertyName("isAvailable")
    public boolean isAvailable() { return isAvailable; }
    @PropertyName("isAvailable")
    public void setAvailable(boolean available) { isAvailable = available; }

    @PropertyName("isPopular")
    public boolean isPopular() { return isPopular; }
    @PropertyName("isPopular")
    public void setPopular(boolean popular) { isPopular = popular; }

    // Support for typo "raating"
    @PropertyName("raating")
    @Exclude
    public double getRaating() { return rating; }
    @PropertyName("raating")
    public void setRaating(Object rating) { this.rating = convertToDouble(rating); }

    @Exclude
    public double getRating() { return rating; }
    @PropertyName("rating")
    public void setRating(Object rating) { this.rating = convertToDouble(rating); }
    @PropertyName("rating")
    public Object getRatingForFirestore() { return rating; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    private double convertToDouble(Object value) {
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return 0.0;
    }
}
