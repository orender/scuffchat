package com.example.scuffchat;

import static android.content.ContentValues.TAG;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

public class ChatRoom extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference userLocationsRef;
    private ListView listViewMessages;
    private Button buttonSend;
    private EditText editTextMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference messagesRef;
    private String chatId;
    private CustomAdapter adapter;
    private ArrayList<Message> messages;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String userId;
    private String userEmail;
    private ConnectivityChangeReceiver receiver;
    private boolean isFirstLoad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userLocationsRef = FirebaseDatabase.getInstance().getReference().child("user_locations");
        receiver = new ConnectivityChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);

        //set notification for when user get dropchat
        Intent serviceIntent = new Intent(this, FirebaseListenerWaterdropService.class);
        startService(serviceIntent);

        Button buttonLeaveChat = findViewById(R.id.buttonLeaveChat);
        buttonLeaveChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveChat();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            userEmail = user.getEmail();
        } else {
            // User is not signed in, handle this case accordingly
        }

        listViewMessages = findViewById(R.id.listViewMessages);
        messages = new ArrayList<>();
        adapter = new CustomAdapter(this, messages);
        listViewMessages.setAdapter(adapter);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSendMessage);



        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        chatId = getIntent().getStringExtra("chatId");
        if (chatId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference chatRef = database.getReference("chat_rooms").child(chatId);
            messagesRef = chatRef.child("messages");
            isFirstLoad = true;
            messagesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(isFirstLoad)
                    {
                        isFirstLoad = false;

                        messages.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Retrieve the message data
                            String userId = snapshot.child("userId").getValue(String.class);
                            String userEmail = snapshot.child("userEmail").getValue(String.class);
                            String imageDataBase64 = snapshot.child("imageData").getValue(String.class);
                            String timestamp = snapshot.child("timestamp").getValue(String.class);

                            // Download the image asynchronously
                            DownloadImageTask downloadTask = new DownloadImageTask(new BitmapDownloadCallback() {
                                @Override
                                public void onBitmapDownloaded(Bitmap bitmap) {
                                    // Create a Message object with the retrieved data
                                    Message message = new Message(userId, userEmail, bitmap, Long.parseLong(timestamp));

                                    // Handle the received message
                                    handleMessage(message);
                                }

                                @Override
                                public void onBitmapDownloadFailed(String errorMessage) {
                                    // Handle download failure
                                    Log.e(TAG, errorMessage);
                                }
                            });
                            downloadTask.execute(imageDataBase64);
                        }
                    }
                    else{
                        isFirstLoad = true;
                        Intent intent = new Intent(ChatRoom.this, IntervalLoad.class);
                        intent.putExtra("chatId", chatId);
                        startActivity(intent);
                        finish();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    private void handleMessage(Message message) {
        // Add the message to your data source
        messages.add(message);

        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();

        // Scroll to the last message
        listViewMessages.smoothScrollToPosition(messages.size() - 1);
    }

    private void leaveChat() {
        Intent intent = new Intent(ChatRoom.this, ChatsList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Optional: Close the current activity
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


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    sendMessage(imageBitmap);
                }
            }
        }
    }

    private void sendMessage(Bitmap imageBitmap) {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear the EditText
        editTextMessage.setText("");

        // Overlay text on the image
        Bitmap messageImage = overlayTextOnImage(imageBitmap, messageText);

        // Upload the combined image to Firebase Storage
        //uploadImageToFirebase(messageImage);
        FirebaseUploadTask uploadTask = new FirebaseUploadTask(userId, userEmail, chatId);
        uploadTask.execute(messageImage);

        uploadUserLocation();
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
}