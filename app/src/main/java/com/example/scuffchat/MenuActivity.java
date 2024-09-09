package com.example.scuffchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MenuActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference userLocationsRef;

    // Method to upload user's current location to Firebase
    private void uploadUserLocation() {
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permissions not granted, request them
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Location retrieved successfully, upload to Firebase
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        String userEmail = currentUser.getEmail();
                        DatabaseReference userLocationRef = userLocationsRef.child(userId);
                        userLocationRef.child("email").setValue(userEmail);
                        userLocationRef.child("latitude").setValue(location.getLatitude());
                        userLocationRef.child("longitude").setValue(location.getLongitude());
                    }
                } else {
                    // Unable to retrieve location
                    Log.d("Location", "Last known location is null");
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userLocationsRef = FirebaseDatabase.getInstance().getReference().child("user_locations");
        uploadUserLocation();

        // Initialize buttons
        Button btnGlobalChats = findViewById(R.id.btnGlobalChats);
        Button btnsendDropchat = findViewById(R.id.btnSendDropchat);
        Button btnDropchatFlow = findViewById(R.id.btnDropchatFlow);
        Button btnUserLocations = findViewById(R.id.btnUserLocations);
        Button btnLogout = findViewById(R.id.btnLogout);

        //set notification for when user get dropchat
        Intent serviceIntent = new Intent(this, FirebaseListenerWaterdropService.class);
        startService(serviceIntent);

        // Set click listeners for buttons
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to leave the chat
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                mAuth.signOut();
            }
        });

        // Set click listeners for buttons
        btnGlobalChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch Global Chats activity
                startActivity(new Intent(MenuActivity.this, ChatsList.class));
                finish();
            }
        });

        btnsendDropchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch Local Dropchat activity
                startActivity(new Intent(MenuActivity.this, DropchatSend.class));
            }
        });

        btnDropchatFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch Local Dropchat activity
                startActivity(new Intent(MenuActivity.this, DropchatFlow.class));
            }
        });

        btnUserLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch User Locations activity
                startActivity(new Intent(MenuActivity.this, UserLocations.class));
            }
        });
    }
}
