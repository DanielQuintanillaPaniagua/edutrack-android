package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etCorreo, etCarnet, etPassword;
    RadioGroup rgRol;
    Button btnRegistrar;
    TextView tvYaTengoCuenta;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        dbHelper = new DatabaseHelper(this);

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etCarnet = findViewById(R.id.etCarnet);
        etPassword = findViewById(R.id.etPassword);
        rgRol = findViewById(R.id.rgRol);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvYaTengoCuenta = findViewById(R.id.tvYaTengosCuenta);

        btnRegistrar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();
            String carnet = etCarnet.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validaciones
            if (nombre.isEmpty() || correo.isEmpty() ||
                    carnet.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (rgRol.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Selecciona un rol",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String rol = rgRol.getCheckedRadioButtonId() == R.id.rbDocente
                    ? "docente" : "estudiante";

            // Guardar sesión en SQLite
            dbHelper.guardarSesion(nombre, correo, rol, carnet);

            Toast.makeText(this, "¡Cuenta creada! Bienvenido, " + nombre,
                    Toast.LENGTH_SHORT).show();

            // Ir al dashboard
            Intent intent = new Intent(RegistroActivity.this,
                    DashboardDocenteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Volver al login
        tvYaTengoCuenta.setOnClickListener(v -> {
            finish();
        });
    }
}