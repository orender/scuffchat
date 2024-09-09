package com.example.scuffchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewChat extends AppCompatActivity {
    private EditText editTextChatName;
    private DatabaseReference chatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        Button buttonLeaveChat = findViewById(R.id.LeaveNewChat);
        buttonLeaveChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveNewChat();
            }
        });

        // Initialize views
        editTextChatName = findViewById(R.id.editText_chat_name);
        Button buttonCreateChat = findViewById(R.id.button_create_chat);

        // Set click listener for create chat button
        buttonCreateChat.setOnClickListener(v -> createChat());

        // Initialize Firebase database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        chatsRef = database.getReference("chat_rooms");
    }

    // Method to create a new chat room
    private void createChat() {
        // Get chat name from EditText
        String chatName = editTextChatName.getText().toString().trim();
        // Check if chat name is not empty
        if (!TextUtils.isEmpty(chatName)) {
            // Generate unique chat ID
            String chatId = chatsRef.push().getKey();
            // Store chat name under "name" node with generated chat ID
            chatsRef.child(chatName).child("chatId").setValue(chatId);
            // Display success message
            Toast.makeText(getApplicationContext(), "Chat created successfully!", Toast.LENGTH_SHORT).show();
            // Close activity
            finish();
        } else {
            // Display error message if chat name is empty
            Toast.makeText(getApplicationContext(), "Please enter a chat name", Toast.LENGTH_SHORT).show();
        }
    }

    private void leaveNewChat() {
        Intent intent = new Intent(NewChat.this, ChatsList.class);
        startActivity(intent);
        finish();
    }
}
