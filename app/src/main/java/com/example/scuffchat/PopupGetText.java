package com.example.scuffchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class PopupGetText extends AppCompatActivity {

    EditText editText;
    Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popup_get_text);

        // Initialize views
        editText = findViewById(R.id.editText_chat_name_popup);
        confirmButton = findViewById(R.id.button_tke_photo_popup);

        // Set click listener for confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from EditText
                String inputText = editText.getText().toString();

                // Pass the text back to the calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("inputText", inputText);
                setResult(Activity.RESULT_OK, resultIntent);
                finish(); // Close the activity
            }
        });
    }
}
