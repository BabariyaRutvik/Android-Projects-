package com.example.quickbazaar.BazaarModel;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String name;
    private String iconUrl;
    private int priority;

    public Category() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @PropertyName("iconUrl")
    public String getIconUrl() { return iconUrl; }
    
    @PropertyName("iconUrl")
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
