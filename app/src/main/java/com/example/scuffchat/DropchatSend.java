package com.example.scuffchat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DropchatSend extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_POPUP = 2;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    private FirebaseAuth mAuth;
    private DropchatListAdapter adapter;
    private ListView listViewDropchats;
    private ArrayList<String> LocalUsersEmails;
    private ArrayList<String> LocalUsersUids;
    private ConnectivityChangeReceiver receiver;
    private SeekBar distanceSeekBar;
    private TextView distanceTextView;
    private DatabaseReference userLocationsRef;
    private String MessageText;
    private Bitmap MessageImage;
    private String targetUserId;

    private float MaxDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dropchat_send);

        userLocationsRef = FirebaseDatabase.getInstance().getReference().child("user_locations");
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button buttonLeaveChat = findViewById(R.id.LeaveDropchatSend);
        buttonLeaveChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveDropchatList();
            }
        });

        Button buttonRefresh = findViewById(R.id.RefreshLocalButton);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryNearbyUsers();
            }
        });

        receiver = new ConnectivityChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);

        listViewDropchats = findViewById(R.id.listViewDropchats);
        LocalUsersEmails = new ArrayList<>();
        LocalUsersUids = new ArrayList<>();
        adapter = new DropchatListAdapter(this, LocalUsersEmails);
        listViewDropchats.setAdapter(adapter);

        distanceSeekBar = findViewById(R.id.distanceSeekBar);
        distanceTextView = findViewById(R.id.distanceText);
        MaxDistance = 0;

        // Set the initial text for the distance TextView
        distanceTextView.setText("0 km");

        // Set a listener to update the distance TextView when SeekBar progress changes
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the distance TextView with the current progress value
                MaxDistance = progress*1000;
                distanceTextView.setText(progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing when SeekBar tracking starts
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing when SeekBar tracking stops
            }
        });

        listViewDropchats.setOnItemClickListener((parent, view, position, id) -> {
            targetUserId = LocalUsersUids.get(position);
            startActivityForResult(new Intent(DropchatSend.this, PopupGetText.class), REQUEST_CODE_POPUP);
        });
    }

    private void sendMessage(Bitmap imageBitmap, String messageText, String messageTarget) {
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Overlay text on the image
        Bitmap messageImage = overlayTextOnImage(imageBitmap, messageText);

        // Upload the combined image to Firebase Storage
        //uploadImageToFirebase(messageImage);
        FirebaseUploadDrop uploadTask = new FirebaseUploadDrop(messageTarget);
        uploadTask.execute(messageImage);

        Toast.makeText(this, "Drop sent!", Toast.LENGTH_SHORT).show();

        uploadUserLocation();
    }

    private void uploadUserLocation() {
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private Bitmap overlayTextOnImage(Bitmap image, String text) {
        // Create a mutable bitmap with the same dimensions as the original image
        Bitmap bitmap = image.copy(Bitmap.Config.ARGB_8888, true);

        // Create a canvas to draw on the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Create a Paint object for drawing text
        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // Set text color
        paint.setAntiAlias(true); // Enable anti-aliasing

        // Calculate maximum text size based on 1/8 of the screen height
        int maxTextSize = bitmap.getHeight() / 8;
        paint.setTextSize(maxTextSize); // Set initial text size

        // Measure text bounds to determine if it fits within the image
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);

        // If the text height exceeds 1/8 of the screen height, decrease the font size
        while (textBounds.height() > bitmap.getHeight() / 16) {
            maxTextSize--; // Decrease text size
            paint.setTextSize(maxTextSize); // Update text size
            paint.getTextBounds(text, 0, text.length(), textBounds); // Re-measure text bounds
        }

        // Calculate text position (centered horizontally and vertically)
        int xPos = (bitmap.getWidth() - textBounds.width()) / 2;
        int yPos = (bitmap.getHeight() + textBounds.height()) / 2;

        // Draw the text on the canvas
        canvas.drawText(text, xPos, yPos, paint);

        return bitmap;
    }

    private void leaveDropchatList() {
        // Implement logic to leave the chat
        // For example, navigate back to the chat list activity
        Intent intent = new Intent(DropchatSend.this, MenuActivity.class);
        startActivity(intent);
        finish(); // Optional: Close the current activity
    }

    private void queryNearbyUsers() {
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permissions not granted, request them
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LocalUsersEmails.clear();
                    LocalUsersUids.clear();
                    // Location retrieved successfully, proceed with querying nearby users
                    userLocationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String userId = userSnapshot.getKey();
                                if (!userId.equals(mAuth.getCurrentUser().getUid())) { // Exclude current user
                                    String userEmail = userSnapshot.child("email").getValue(String.class);
                                    double latitude = userSnapshot.child("latitude").getValue(Double.class);
                                    double longitude = userSnapshot.child("longitude").getValue(Double.class);
                                    Location userLocation = new Location("");
                                    userLocation.setLatitude(latitude);
                                    userLocation.setLongitude(longitude);
                                    float distance = location.distanceTo(userLocation); // Distance in meters from current user's location
                                    // You can adjust the distance threshold based on your requirements
                                    if (distance <= MaxDistance) {
                                        // Add the user's email and UID to the list
                                        LocalUsersEmails.add(userEmail);
                                        LocalUsersUids.add(userId);
                                    }
                                }
                            }
                            // Notify the adapter that the data set has changed
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("Firebase", "Error: " + databaseError.getMessage());
                        }
                    });
                } else {
                    // Unable to retrieve location
                    Log.d("Location", "Last known location is null");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_POPUP && resultCode == Activity.RESULT_OK) {
            MessageText = data.getStringExtra("inputText");
            dispatchTakePictureIntent();
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    MessageImage = imageBitmap;

                    sendMessage(MessageImage, MessageText, targetUserId);
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

}
