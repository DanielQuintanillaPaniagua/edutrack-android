package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Usuario;

public class LoginActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    RadioButton rbDocente, rbEstudiante;

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

        rbDocente    = findViewById(R.id.rbDocente);
        rbEstudiante = findViewById(R.id.rbEstudiante);
        Button btnLogin       = findViewById(R.id.btnLogin);
        TextView tvRegistrate = findViewById(R.id.tvRegistro);

        // Controlar selección mutua manualmente
        rbDocente.setOnClickListener(v -> rbEstudiante.setChecked(false));
        rbEstudiante.setOnClickListener(v -> rbDocente.setChecked(false));

        // Tarjeta Docente
        findViewById(R.id.cardDocente).setOnClickListener(v -> {
            rbDocente.setChecked(true);
            rbEstudiante.setChecked(false);
        });

        // Tarjeta Estudiante
        findViewById(R.id.cardEstudiante).setOnClickListener(v -> {
            rbEstudiante.setChecked(true);
            rbDocente.setChecked(false);
        });

        tvRegistrate.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));

        btnLogin.setOnClickListener(v -> {
            String rol;
            if (rbDocente.isChecked()) {
                rol = "docente";
            } else if (rbEstudiante.isChecked()) {
                rol = "estudiante";
            } else {
                Toast.makeText(this, "Seleccioná un rol", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.putExtra("rol", rol);
            startActivity(intent);
        });
    }

    private void irAlDashboard() {
        Usuario usuario = dbHelper.obtenerUsuarioSesion();
        Class<?> destino = (usuario != null && "docente".equals(usuario.getRol()))
                ? DashboardDocenteActivity.class
                : DashboardEstudianteActivity.class;

        Intent intent = new Intent(this, destino);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}