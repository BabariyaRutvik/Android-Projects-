package com.example.quickbazaar.BazaarModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class ChatMessage {
    public static final int TYPE_USER = 1;
    public static final int TYPE_AI = 2;

    private String text;
    private int type;
    @ServerTimestamp
    private Timestamp timestamp;

    public ChatMessage() {
        // Required for Firestore
    }

    public ChatMessage(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
