package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.edutrack.edutrack.R;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardEstudianteActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    FloatingActionButton fabEscanear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_estudiante);

        bottomNav   = findViewById(R.id.bottomNavEstudiante);
        fabEscanear = findViewById(R.id.fabEscanear);

        // Fragment inicial
        cargarFragment(new InicioEstFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if      (id == R.id.nav_inicio_est)     fragment = new InicioEstFragment();
            else if (id == R.id.nav_materias_est)   fragment = new MateriasEstFragment();
            else if (id == R.id.nav_asistencia_est) fragment = new AsistenciaEstFragment();
            else if (id == R.id.nav_perfil_est)     fragment = new PerfilEstFragment();
            if (fragment != null) cargarFragment(fragment);
            return true;
        });
        fabEscanear.setOnClickListener(v -> {
            Intent intent = new Intent(this, EscanerQRActivity.class);
            startActivity(intent);
        });
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorEstudiante, fragment)
                .commit();
    }
}