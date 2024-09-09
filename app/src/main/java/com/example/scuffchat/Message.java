package com.example.scuffchat;

import android.graphics.Bitmap;

public class Message {
    private String userId;
    private String userEmail;
    private String text;
    private Bitmap imageData; // Bitmap for image message
    private long timestamp;

    // Empty constructor required for Firebase
    public Message() {
    }

    // Constructor for text message
    public Message(String userId, String userEmail, String text, long timestamp) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Constructor for image message
    public Message(String userId, String userEmail, Bitmap imageBitmap, long timestamp) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.imageData = imageBitmap;
        this.timestamp = timestamp;
    }

    // Getters and setters

    // Getter for userId
    public String getUserId() {
        return userId;
    }

    // Setter for userId
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter for userEmail
    public String getUserEmail() {
        return userEmail;
    }

    // Setter for userEmail
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Getter for text message
    public String getText() {
        return text;
    }

    // Setter for text message
    public void setText(String text) {
        this.text = text;
    }

    // Getter for image bitmap
    public Bitmap getImageBitmap() {
        return imageData;
    }

    // Setter for image bitmap
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageData = imageBitmap;
    }

    // Getter for timestamp
    public long getTimestamp() {
        return timestamp;
    }

    // Setter for timestamp
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
