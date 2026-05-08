package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


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

        // Si ya hay sesión activa, ir directo al dashboard
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
            // Por ahora guardamos datos de prueba
            // Después esto vendrá de CouchDB
            dbHelper.guardarSesion(
                    "Ana García",
                    "ana.garcia@ugb.edu.sv",
                    "docente",
                    "DOC-001"
            );

            Toast.makeText(this, "¡Bienvenida, Ana García!", Toast.LENGTH_SHORT).show();
            irAlDashboard();
        });
    }

    private void irAlDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardDocenteActivity.class);
        startActivity(intent);
        finish();
    }
}