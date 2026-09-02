package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button smsBtn = findViewById(R.id.smsBtn);

        smsBtn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, SmsActivity.class));
        });
    }
}