package com.example.quickbazaar.BazaarModel;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private List<CartItem> items;
    private double totalAmount;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String city;
    private String paymentMethod;
    private long timestamp;
    private String status; // e.g . Pending, delivered , and Confirmed

    // default constructor
    public Order() {
    }
    // parameterised constructor

    public Order(String orderId, String userId, List<CartItem> items, double totalAmount, String fullName, String phoneNumber, String address, String city, String paymentMethod, long timestamp, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
        this.status = status;
    }




    // Getters and Setters


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
