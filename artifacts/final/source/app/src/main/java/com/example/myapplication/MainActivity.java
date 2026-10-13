package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Open the roster without implying that this prototype has a working login.
        findViewById(R.id.openRosterButton).setOnClickListener(
                view -> startActivity(new Intent(this, DashboardActivity.class)));
    }
}
