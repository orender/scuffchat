package com.example.scuffchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        TextView textViewSignIn = findViewById(R.id.textViewLogIn);

        // Set click listener for sign-in text
        textViewSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SignUp.this, MainActivity.class));
            finish();
        });

        // Initialize Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // Set click listener for sign-up button
        buttonSignUp.setOnClickListener(v -> signUp());
    }

    // Method to handle user sign-up
    private void signUp() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Check if email field is empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password field is empty
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Enter password! make sure it is at least 6 letters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-up successful, navigate to menu activity
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(SignUp.this, MenuActivity.class);
                        startActivity(intent);
                        // Navigate to additional user information setup or other screen
                    } else {
                        // Sign-up failed, display error message
                        Toast.makeText(getApplicationContext(), "Authentication failed! make sure you use a valid email!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
