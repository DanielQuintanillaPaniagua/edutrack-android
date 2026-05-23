package com.example.edutrack.edutrack.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.edutrack.edutrack.R;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.edutrack.edutrack.database.FirebaseManager;


public class DashboardDocenteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_docente);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // Descargar estudiantes de Firebase al iniciar
        FirebaseManager.descargarEstudiantes(this, null);

        // Carga el fragment de inicio por defecto
        cargarFragment(new InicioFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_inicio) {
                fragment = new InicioFragment();
            } else if (item.getItemId() == R.id.nav_materias) {
                fragment = new MateriasFragment();
            } else if (item.getItemId() == R.id.nav_reportes) {
                fragment = new ReportesFragment();
            } else if (item.getItemId() == R.id.nav_historial) {
                fragment = new HistorialFragment();
            } else if (item.getItemId() == R.id.nav_perfil) {
                fragment = new PerfilFragment();
            }

            if (fragment != null) cargarFragment(fragment);
            return true;
        });
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}