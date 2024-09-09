package com.example.scuffchat;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DropchatFlow extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button LeaveChat;
    private TextView NoDropMessagesTv;
    private ImageView DropMessageTv;
    private String userId;
    private boolean CanGetMessage;
    private TextView countdownTimer;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dropchat_flow);

        countdownTimer = findViewById(R.id.countdownTimer);

        CanGetMessage = true;

        mAuth = FirebaseAuth.getInstance();

        NoDropMessagesTv = findViewById(R.id.NoDropMessagesTv);
        DropMessageTv = findViewById(R.id.fullscreenDropchatMessage);

        startNoMessagesAnimation();

        userId = mAuth.getCurrentUser().getUid();

        LeaveChat = findViewById(R.id.LeaveDropchatFlow);
        LeaveChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveDropchatFlow();
            }
        });

        // Listen for messages in Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference messagesRef = database.getReference("Dropchat").child(userId);
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (CanGetMessage) {
                    // Get the image URL from the message
                    String imageUrl = dataSnapshot.child("imageData").getValue(String.class);

                    if (imageUrl != null) {
                        CanGetMessage = false;

                        // Stop animation
                        stopNoMessagesAnimation();

                        // Display the image in full screen
                        displayImageFullScreen(imageUrl);

                        startCountdownTimer();

                        // Remove the message from Firebase after 10 seconds
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DropMessageTv.setVisibility(View.GONE); // Hide the full-screen image
                                startNoMessagesAnimation();
                                messagesRef.child("imageData").removeValue(); // Remove the message from Firebase
                                deleteFileFromStorage(imageUrl);
                                CanGetMessage = true;
                                cancelCountdownTimer();
                            }
                        }, 10000); // 10 seconds
                    }
                } else {
                    // Get the image URL from the message
                    String imageUrl = dataSnapshot.child("imageData").getValue(String.class);
                    deleteFileFromStorage(imageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void leaveDropchatFlow() {
        // Implement logic to leave the chat
        // For example, navigate back to the chat list activity
        Intent intent = new Intent(DropchatFlow.this, MenuActivity.class);
        startActivity(intent);
        finish(); // Optional: Close the current activity
    }

    private void startNoMessagesAnimation() {
        NoDropMessagesTv.setVisibility(View.VISIBLE);
    }

    private void stopNoMessagesAnimation() {
        NoDropMessagesTv.setVisibility(View.GONE);
    }

    private void displayImageFullScreen(String imageUrl) {
        DownloadImageTask downloadTask = new DownloadImageTask(new BitmapDownloadCallback() {
            @Override
            public void onBitmapDownloaded(Bitmap bitmap) {
                // Create a Message object with the retrieved data
                DropMessageTv.setImageBitmap(bitmap);
                DropMessageTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBitmapDownloadFailed(String errorMessage) {
                // Handle download failure
                Log.e(TAG, errorMessage);
            }
        });
        downloadTask.execute(imageUrl);
    }

    // Method to delete a file from Firebase Storage using its URL
    public void deleteFileFromStorage(String fileUrl) {
        // Get reference to the storage service
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from the URL
        StorageReference storageRef = storage.getReferenceFromUrl(fileUrl);

        // Delete the file
        storageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // File deleted successfully
                    Log.d(TAG, "File deleted successfully");
                })
                .addOnFailureListener(exception -> {
                    // Uh-oh, an error occurred!
                    Log.e(TAG, "Error deleting file: " + exception.getMessage());
                });
    }

    private void startCountdownTimer() {
        countdownTimer.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(10000, 1000) { // 10 seconds with interval of 1 second
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                countdownTimer.setText(String.valueOf(secondsRemaining));
            }

            public void onFinish() {
                countdownTimer.setText("10");
            }
        }.start();
    }

    private void cancelCountdownTimer() {
        countdownTimer.setText("10");
        if (timer != null) {
            timer.cancel();
        }
    }
}
