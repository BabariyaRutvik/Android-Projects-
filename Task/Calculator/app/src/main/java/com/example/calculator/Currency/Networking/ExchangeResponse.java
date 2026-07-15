package com.example.calculator.Currency.Networking;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ExchangeResponse
{
    @SerializedName("base")
    private String baseCurrency;

    @SerializedName("rates")
    private Map<String,Double> rates;

    @SerializedName("date")
    private String date;

    // getter and setter

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
}
