package com.example.scuffchat;

public class Chat {
    private String name;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(Chat.class)
    }

    public Chat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}