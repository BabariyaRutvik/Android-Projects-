package com.example.bharatbuzz.NewsModel;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Source implements Serializable
{
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    // constructor
    public Source(String id, String name) {
        this.id = id;
        this.name = name;
    }
    // getter and setter
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
