package com.example.edutrack.edutrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.adapters.MateriaEstAdapter;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Usuario;

import java.util.List;

public class MateriasEstFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_materias_est_fragment, container, false);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        Usuario usuario = db.obtenerUsuarioSesion();

        RecyclerView rv = view.findViewById(R.id.rvMateriasEst);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        TextView tvVacio = view.findViewById(R.id.tvVacioMaterias);

        if (usuario != null) {
            List<Object[]> resumen = db.obtenerResumenAsistenciaEstudiante(usuario.getId());

            if (resumen.isEmpty()) {
                tvVacio.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                tvVacio.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                rv.setAdapter(new MateriaEstAdapter(resumen));
            }
        }

        return view;
    }
}