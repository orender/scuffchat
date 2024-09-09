package com.example.scuffchat;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class FirebaseUploadDrop extends AsyncTask<Bitmap, Void, Void> {

    private static final String TAG = "UploadImageTask";

    private String TargetUserId;

    public FirebaseUploadDrop(String userId) {
        this.TargetUserId = userId;
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        // Get a reference to the Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Bitmap imageBitmap = bitmaps[0];

        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Dropchat").child(TargetUserId);

        // Generate a unique filename for the image
        String filename = UUID.randomUUID().toString() + ".jpg";

        // Create a reference to the image file in Firebase Storage
        StorageReference imageRef = storageRef.child("images/" + filename);

        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image data to Firebase Storage
        UploadTask uploadTask = imageRef.putBytes(data);

        // Register observers to listen for upload progress or errors
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.e(TAG, "Upload failed: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Task completed successfully
                Log.d(TAG, "Upload successful");

                // Get the download URL for the uploaded image
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // URI of the uploaded image
                        String imageUrl = uri.toString();

                        // Add the image URL to the database
                        databaseRef.child("imageData").setValue(imageUrl);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors getting the download URL
                        Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                    }
                });

            }
        });

        return null;
    }
}
