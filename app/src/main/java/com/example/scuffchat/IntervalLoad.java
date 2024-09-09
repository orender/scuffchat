package com.example.scuffchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class IntervalLoad extends AppCompatActivity {

    private static final int DELAY_MILLISECONDS = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_interval_load);

        // Delay transition to another activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start ChatRoom activity
                Intent intent = new Intent(IntervalLoad.this, ChatRoom.class);
                // Pass chatId to ChatRoom activity
                intent.putExtra("chatId", getIntent().getStringExtra("chatId"));
                startActivity(intent);
                // Finish this activity
                finish();
            }
        }, DELAY_MILLISECONDS);
    }
}
