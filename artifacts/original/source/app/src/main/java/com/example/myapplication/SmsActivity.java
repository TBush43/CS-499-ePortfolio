package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SmsActivity extends AppCompatActivity {

    private TextView statusText;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    statusText.setText("SMS permission granted. Goal weight notifications are enabled.");
                } else {
                    statusText.setText("SMS permission denied. The app will still work, but notifications are disabled.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        Button allowSmsBtn = findViewById(R.id.allowSmsBtn);
        Button denySmsBtn = findViewById(R.id.denySmsBtn);
        statusText = findViewById(R.id.statusText);

        allowSmsBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                statusText.setText("SMS permission already granted. Goal weight notifications are enabled.");
            } else {
                requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            }
        });

        denySmsBtn.setOnClickListener(v -> {
            statusText.setText("SMS permission denied. The app will still work, but notifications are disabled.");
        });
    }
}