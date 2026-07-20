package com.example.calculator.Model;

public class UnitItem {
    private final String name;
    private final String code;
    private final double factor; // Multiplier relative to the base unit

    public UnitItem(String name, String code, double factor) {
        this.name = name;
        this.code = code;
        this.factor = factor;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public double getFactor() {
        return factor;
    }
}
