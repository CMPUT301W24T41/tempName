package com.example.eventsigninapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class MessageActivity extends AppCompatActivity {
    EditText messageTitle, messageBody;
    Button sendButton, backButton, sendToButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setReferences();
    }

    private void setReferences() {
        messageTitle = findViewById(R.id.messageTitle);
        messageBody = findViewById(R.id.messageBody);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        sendToButton = findViewById(R.id.sendToButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            // Navigate back to MainActivity
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                Intent intent = new Intent(MessageActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish the MessageActivity
            }
        });
        sendToButton.setOnClickListener(new View.OnClickListener() {
            // Open a view to select what users to send the message to
            @Override
            public void onClick(View v) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send the message
                sendNotification(messageTitle.getText().toString(), messageBody.getText().toString());
            }
        });
    }

    private void sendNotification(String title, String body) {
        //Send the notification to the server

        String token = "fSHonvpuReC8-rFMMUb0zY";
    }
}