package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edutrack.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.database.FirebaseManager;

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

        dbHelper        = new DatabaseHelper(this);
        etNombre        = findViewById(R.id.etNombre);
        etCorreo        = findViewById(R.id.etCorreo);
        etCarnet        = findViewById(R.id.etCarnet);
        etPassword      = findViewById(R.id.etPassword);
        rgRol           = findViewById(R.id.rgRol);
        btnRegistrar    = findViewById(R.id.btnRegistrar);
        tvYaTengoCuenta = findViewById(R.id.tvYaTengosCuenta);

        btnRegistrar.setOnClickListener(v -> {
            String nombre   = etNombre.getText().toString().trim();
            String correo   = etCorreo.getText().toString().trim();
            String carnet   = etCarnet.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

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

            android.widget.RadioButton rbEstudiante = findViewById(R.id.rbEstudiante);
            String rol = (rbEstudiante != null && rbEstudiante.isChecked())
                    ? "estudiante" : "docente";

            // 1. Guardar en SQLite local
            dbHelper.registrarUsuario(nombre, correo, password, rol, carnet);

            if (!dbHelper.verificarCredenciales(correo, password)) {
                Toast.makeText(this, "❌ Error al guardar usuario",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Guardar sesión local
            Cursor cursor = dbHelper.obtenerUsuario(correo);
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                dbHelper.guardarSesion(id, nombre, correo, rol, carnet);
            }
            cursor.close();

            // 3. Sincronizar con Firebase (en segundo plano)
            String rolFinal = rol;
            FirebaseManager.registrarUsuario(nombre, correo, password, rol, carnet,
                    new FirebaseManager.AuthCallback() {
                        @Override
                        public void onSuccess(String uid) {
                            // Sincronizado en Firebase ✅
                        }
                        @Override
                        public void onError(String error) {
                            // Falla Firebase pero SQLite ya guardó — app funciona igual
                        }
                    });

            Toast.makeText(this, "¡Cuenta creada! Bienvenido, " + nombre,
                    Toast.LENGTH_SHORT).show();

            Class<?> destino = "docente".equals(rol)
                    ? DashboardDocenteActivity.class
                    : DashboardEstudianteActivity.class;

            Intent intent = new Intent(RegistroActivity.this, destino);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        tvYaTengoCuenta.setOnClickListener(v -> finish());
    }
}