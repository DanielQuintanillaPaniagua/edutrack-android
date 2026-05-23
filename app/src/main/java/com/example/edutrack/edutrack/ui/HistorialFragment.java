package com.example.edutrack.edutrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edutrack.edutrack.R;
import com.example.edutrack.edutrack.adapters.HistorialAdapter;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Asistencia;
import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends Fragment {

    public HistorialFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        RecyclerView rv         = view.findViewById(R.id.rvHistorial);
        Button btnVerMas        = view.findViewById(R.id.btnVerMas);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        Usuario usuario = dbHelper.obtenerUsuarioSesion();
        if (usuario == null) return view;

        sincronizarYCargarHistorial(dbHelper, rv, usuario.getId());

        btnVerMas.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "Mostrando todos los registros", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void sincronizarYCargarHistorial(DatabaseHelper dbHelper,
                                             RecyclerView rv, int docenteId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        List<Materia> materias = dbHelper.obtenerMateriasPorDocente(docenteId);
        List<Integer> ids = new ArrayList<>();
        for (Materia m : materias) ids.add(m.getId());

        if (ids.isEmpty()) {
            cargarHistorial(dbHelper, rv, docenteId);
            return;
        }

        firestore.collection("asistencia")
                .whereIn("materia_id", ids)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        int materiaId = ((Long) doc.get("materia_id")).intValue();
                        int presentes = doc.get("presentes") != null
                                ? ((Long) doc.get("presentes")).intValue() : 0;
                        int total     = doc.get("total") != null
                                ? ((Long) doc.get("total")).intValue() : 0;
                        String fecha  = doc.getString("fecha");

                        dbHelper.actualizarConteoSesion(materiaId, fecha, presentes, total);
                    }
                    cargarHistorial(dbHelper, rv, docenteId);
                })
                .addOnFailureListener(e -> cargarHistorial(dbHelper, rv, docenteId));
    }

    private void cargarHistorial(DatabaseHelper dbHelper, RecyclerView rv,
                                 int docenteId) {
        List<Asistencia> historial = dbHelper.obtenerHistorialPorDocente(docenteId);

        if (historial.isEmpty()) {
            TextView tvVacio = new TextView(requireContext());
            tvVacio.setText("📋 No hay registros de asistencia aún.\nUsa el escáner QR para registrar clases.");
            tvVacio.setTextColor(0xFF90CAF9);
            tvVacio.setTextSize(14f);
            tvVacio.setPadding(48, 48, 48, 0);
            tvVacio.setGravity(android.view.Gravity.CENTER);
            ((ViewGroup) rv.getParent()).addView(tvVacio, 2);
        } else {
            HistorialAdapter adapter = new HistorialAdapter(getContext(), historial);
            rv.setAdapter(adapter);
        }
    }

}