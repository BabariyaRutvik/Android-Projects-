package com.example.calculator.Currency.Networking;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ExchangeResponse
{
    @SerializedName("base")
    private String baseCurrency;

    @SerializedName("rates")
    private Map<String,Double> rates;

    // getter and setter

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
