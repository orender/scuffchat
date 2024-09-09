package com.example.scuffchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private ConnectivityChangeReceiver receiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, navigate to menu activity
            startActivity(new Intent(MainActivity.this, MenuActivity.class));
            finish();
        }

        // Register broadcast receiver for network connectivity changes
        receiver = new ConnectivityChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonSignIn = findViewById(R.id.buttonSignIn);
        TextView textViewSignUp = findViewById(R.id.textViewSignUp);

        // Set click listener for sign-in button
        buttonSignIn.setOnClickListener(v -> signIn());

        // Set click listener for sign-up text
        textViewSignUp.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUp.class));
            finish();
        });
    }

    // Method to handle user sign-in
    private void signIn() {
        // Get email and password from EditText fields
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Check if email field is empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password field is empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in successful, navigate to menu activity
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Sign-in failed, display error message
                        Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
