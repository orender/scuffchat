package com.example.scuffchat;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseListenerWaterdropService extends Service {

    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private ValueEventListener mListener;
    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();

        // Listen for messages in Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference("Dropchat").child(mAuth.getCurrentUser().getUid());

        // Initialize MediaPlayer with the waterdrop sound
        mMediaPlayer = MediaPlayer.create(this, R.raw.waterdrop);

        // Set up ValueEventListener to listen for data changes
        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Play the waterdrop sound when data changes
                String imageUrl = dataSnapshot.child("imageData").getValue(String.class);
                if (imageUrl != null) {
                    playWaterdropSound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        };

        // Add the ValueEventListener to the database reference
        mDatabaseRef.addValueEventListener(mListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void playWaterdropSound() {
        // Check if the MediaPlayer is already playing, stop it and reset
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }

        // Start playing the waterdrop sound
        mMediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the ValueEventListener when the service is destroyed
        if (mDatabaseRef != null && mListener != null) {
            mDatabaseRef.removeEventListener(mListener);
        }

        // Release the MediaPlayer
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
