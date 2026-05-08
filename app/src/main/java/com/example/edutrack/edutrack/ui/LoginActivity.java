package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edutrack.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Button btnIniciarSesion = findViewById(R.id.btnLogin);

        btnIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DashboardDocenteActivity.class);
            startActivity(intent);
            finish();
        });
    }
}