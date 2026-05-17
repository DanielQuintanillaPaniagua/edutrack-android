package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edutrack.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.database.FirebaseManager;

public class LoginFormActivity extends AppCompatActivity {

    EditText etCorreo, etPassword;
    Button btnIngresar;
    TextView tvRolTitulo, tvVolver;
    DatabaseHelper dbHelper;
    String rolSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        dbHelper = new DatabaseHelper(this);
        etCorreo    = findViewById(R.id.etCorreo);
        etPassword  = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvRolTitulo = findViewById(R.id.tvRolTitulo);
        tvVolver    = findViewById(R.id.tvVolver);

        rolSeleccionado = getIntent().getStringExtra("rol");
        tvRolTitulo.setText("Iniciar sesión como " +
                (rolSeleccionado.equals("docente") ? "Docente" : "Estudiante"));

        btnIngresar.setOnClickListener(v -> {
            String correo   = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Intentar login con Firebase primero
            FirebaseManager.loginUsuario(correo, password,
                    new FirebaseManager.AuthCallback() {
                        @Override
                        public void onSuccess(String uid) {
                            // Firebase OK → procesar sesión local
                            procesarLoginLocal(correo, password);
                        }
                        @Override
                        public void onError(String error) {
                            // Sin conexión o error → fallback a SQLite
                            procesarLoginLocal(correo, password);
                        }
                    });
        });

        tvVolver.setOnClickListener(v -> finish());
    }

    private void procesarLoginLocal(String correo, String password) {
        if (dbHelper.verificarCredenciales(correo, password)) {
            Cursor cursor = dbHelper.obtenerUsuario(correo);
            if (cursor.moveToFirst()) {
                int    id     = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String rol    = cursor.getString(cursor.getColumnIndexOrThrow("rol"));
                String carnet = cursor.getString(cursor.getColumnIndexOrThrow("carnet"));
                cursor.close();

                dbHelper.guardarSesion(id, nombre, correo, rol, carnet);
                Toast.makeText(this, "¡Bienvenido, " + nombre + "!",
                        Toast.LENGTH_SHORT).show();

                Class<?> destino = rol.equals("docente")
                        ? DashboardDocenteActivity.class
                        : DashboardEstudianteActivity.class;

                Intent intent = new Intent(LoginFormActivity.this, destino);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                cursor.close();
            }
        } else {
            Toast.makeText(this, "Correo o contraseña incorrectos",
                    Toast.LENGTH_SHORT).show();
        }
    }
}