package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button login = findViewById(R.id.loginBtn);
        Button createAccount = findViewById(R.id.createBtn);

        login.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        });

        createAccount.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        });
    }
}