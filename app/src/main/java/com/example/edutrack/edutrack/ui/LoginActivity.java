package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        if (dbHelper.haySesionActiva()) {
            irAlDashboard();
            return;
        }

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegistrate = findViewById(R.id.tvRegistro);

        tvRegistrate.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, LoginFormActivity.class);
            intent.putExtra("rol", "docente");
            startActivity(intent);
        });
    }

    private void irAlDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardDocenteActivity.class);
        startActivity(intent);
        finish();
    }
}