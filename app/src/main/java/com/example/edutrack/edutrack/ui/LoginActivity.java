package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

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

        // ✅ Si hay sesión activa → prompt de huella automático
        if (dbHelper.haySesionActiva()) {
            if (hayHuellaDisponible()) {
                mostrarPromptHuella();
            } else {
                irAlDashboard();
            }
            return;
        }

        rbDocente    = findViewById(R.id.rbDocente);
        rbEstudiante = findViewById(R.id.rbEstudiante);
        Button btnLogin       = findViewById(R.id.btnLogin);
        TextView tvRegistrate = findViewById(R.id.tvRegistro);

        rbDocente.setOnClickListener(v -> rbEstudiante.setChecked(false));
        rbEstudiante.setOnClickListener(v -> rbDocente.setChecked(false));

        findViewById(R.id.cardDocente).setOnClickListener(v -> {
            rbDocente.setChecked(true);
            rbEstudiante.setChecked(false);
        });

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
                Toast.makeText(this, "Seleccioná un rol",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.putExtra("rol", rol);
            startActivity(intent);
        });
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
                .setSubtitle("Confirma tu identidad para continuar")
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
                        if (code == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                                code == BiometricPrompt.ERROR_USER_CANCELED) {
                            // Cerrar sesión y mostrar login normal
                            dbHelper.cerrarSesion();
                            recreate();
                        }
                    }
                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(LoginActivity.this,
                                "Huella no reconocida", Toast.LENGTH_SHORT).show();
                    }
                });

        prompt.authenticate(promptInfo);
    }

    // ── Dashboard ────────────────────────────────────────
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