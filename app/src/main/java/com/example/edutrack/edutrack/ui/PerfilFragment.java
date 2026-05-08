package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;

public class PerfilFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        Button btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        btnCerrarSesion.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro que deseas cerrar sesión?")
                    .setPositiveButton("Sí, salir", (dialog, which) -> {
                        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                        dbHelper.cerrarSesion();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        return view;
    }
}