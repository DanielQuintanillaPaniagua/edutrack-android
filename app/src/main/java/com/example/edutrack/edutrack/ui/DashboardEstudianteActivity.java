package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.edutrack.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.database.FirebaseManager;
import com.example.edutrack.edutrack.models.Usuario;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardEstudianteActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    FloatingActionButton fabEscanear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_estudiante);

        bottomNav = findViewById(R.id.bottomNavEstudiante);
        fabEscanear = findViewById(R.id.fabEscanear);

        DatabaseHelper db = new DatabaseHelper(this);
        Usuario sesion = db.obtenerUsuarioSesion();

        if (sesion != null) {

            final String correo = sesion.getCorreo();

            // 1. Descargar materias
            FirebaseManager.descargarMaterias(
                    this,
                    new FirebaseManager.SyncCallback() {

                        @Override
                        public void onSuccess() {

                            // 2. Descargar asistencia
                            FirebaseManager.descargarAsistenciaDocente(
                                    DashboardEstudianteActivity.this,
                                    new FirebaseManager.SyncCallback() {

                                        @Override
                                        public void onSuccess() {

                                            // 3. Descargar inscripciones del estudiante
                                            FirebaseManager.descargarInscripcionesEstudiante(
                                                    DashboardEstudianteActivity.this,
                                                    correo,
                                                    new FirebaseManager.SyncCallback() {

                                                        @Override
                                                        public void onSuccess() {

                                                            runOnUiThread(() ->
                                                                    cargarFragment(new InicioEstFragment()));
                                                        }

                                                        @Override
                                                        public void onError(String error) {

                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onError(String error) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
        }

        // Fragment inicial
        cargarFragment(new InicioEstFragment());

        // Navegación inferior
        bottomNav.setOnItemSelectedListener(item -> {

            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_inicio_est) {
                fragment = new InicioEstFragment();

            } else if (id == R.id.nav_materias_est) {
                fragment = new MateriasEstFragment();

            } else if (id == R.id.nav_asistencia_est) {
                fragment = new AsistenciaEstFragment();

            } else if (id == R.id.nav_perfil_est) {
                fragment = new PerfilEstFragment();
            }

            if (fragment != null) {
                cargarFragment(fragment);
            }

            return true;
        });

        // Botón QR
        fabEscanear.setOnClickListener(v ->
                startActivity(new Intent(
                        DashboardEstudianteActivity.this,
                        EscanerQRActivity.class
                )));
    }

    private void cargarFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorEstudiante, fragment)
                .commit();
    }
}