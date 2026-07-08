package com.example.calculator.Database;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class HistoryItem {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String expression;
    private String result;
    private Long timestamp;

    // constructor


    public HistoryItem(int id, String expression, String result, Long timestamp) {
        this.id = id;
        this.expression = expression;
        this.result = result;
        this.timestamp = timestamp;
    }

    @androidx.room.Ignore
    public HistoryItem(String expression, String result, Long timestamp) {
        this.expression = expression;
        this.result = result;
        this.timestamp = timestamp;
    }



    // getter and Setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}