package com.example.calculator.Currency;

public class CurrencyModel
{
    private String currencyCode;
    private String currencyName;
    private String countryName;
    private String countryCode;
    private double rate = 1.0;

    // constructor

    public CurrencyModel(String currencyCode, String currencyName, String countryName, String countryCode, double rate) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.rate = rate;
    }
    // getter and setter

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
