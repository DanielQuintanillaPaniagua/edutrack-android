package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.edutrack.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.database.FirebaseManager;
import com.example.edutrack.edutrack.models.Usuario;

public class LoginFormActivity extends AppCompatActivity {

    private Button btnHuella;
    private DatabaseHelper db;
    EditText etCorreo, etPassword;
    Button btnIngresar;
    TextView tvRolTitulo, tvVolver;
    DatabaseHelper dbHelper;
    String rolSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        dbHelper        = new DatabaseHelper(this);
        db              = new DatabaseHelper(this);
        etCorreo        = findViewById(R.id.etCorreo);
        etPassword      = findViewById(R.id.etPassword);
        btnIngresar     = findViewById(R.id.btnIngresar);
        tvRolTitulo     = findViewById(R.id.tvRolTitulo);
        tvVolver        = findViewById(R.id.tvVolver);
        btnHuella       = findViewById(R.id.btnHuella);

        // ── Huella ──────────────────────────────────────
        if (db.haySesionActiva() && hayHuellaDisponible()) {
            btnHuella.setVisibility(View.VISIBLE);
        }
        btnHuella.setOnClickListener(v -> mostrarPromptHuella());

        // ── Título según rol ─────────────────────────────
        rolSeleccionado = getIntent().getStringExtra("rol");
        tvRolTitulo.setText("Iniciar sesión como " +
                (rolSeleccionado.equals("docente") ? "Docente" : "Estudiante"));

        // ── Login normal ─────────────────────────────────
        btnIngresar.setOnClickListener(v -> {
            String correo   = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseManager.loginUsuario(correo, password,
                    new FirebaseManager.AuthCallback() {
                        @Override
                        public void onSuccess(String uid) {
                            procesarLoginLocal(correo, password);
                        }
                        @Override
                        public void onError(String error) {
                            procesarLoginLocal(correo, password);
                        }
                    });
        });

        tvVolver.setOnClickListener(v -> finish());
    }

    // ── Biométrico ───────────────────────────────────────
    private boolean hayHuellaDisponible() {
        BiometricManager bm = BiometricManager.from(this);
        return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void mostrarPromptHuella() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("EduTrack")
                .setSubtitle("Confirma tu identidad")
                .setNegativeButtonText("Usar contraseña")
                .build();

        BiometricPrompt prompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            BiometricPrompt.AuthenticationResult result) {
                        irAlDashboard();
                    }
                    @Override
                    public void onAuthenticationError(int code, CharSequence msg) {
                        if (code != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                code != BiometricPrompt.ERROR_USER_CANCELED) {
                            Toast.makeText(LoginFormActivity.this,
                                    "Error: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(LoginFormActivity.this,
                                "Huella no reconocida", Toast.LENGTH_SHORT).show();
                    }
                });

        prompt.authenticate(promptInfo);
    }

    private void irAlDashboard() {
        Usuario sesion = db.obtenerUsuarioSesion();
        if (sesion == null) return;

        Intent intent;
        if ("docente".equalsIgnoreCase(sesion.getRol())) {
            intent = new Intent(this, DashboardDocenteActivity.class);
        } else {
            intent = new Intent(this, DashboardEstudianteActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ── Login local ──────────────────────────────────────
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