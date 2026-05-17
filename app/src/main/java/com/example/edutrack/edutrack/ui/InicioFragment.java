package com.example.edutrack.edutrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.Intent;
import com.example.edutrack.edutrack.ui.GenerarQRActivity;

public class InicioFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());

        TextView tvSaludo          = view.findViewById(R.id.tvSaludo);
        TextView tvFecha           = view.findViewById(R.id.tvFecha);
        LinearLayout llMaterias    = view.findViewById(R.id.llMateriasResumen);
        TextView tvSinMaterias     = view.findViewById(R.id.tvSinMaterias);

        // ── Saludo con nombre real ──────────────────────
        Usuario usuario = dbHelper.obtenerUsuarioSesion();
        if (usuario != null) {
            String saludo = obtenerSaludo() + ", " + usuario.getNombre();
            tvSaludo.setText(saludo);
        }

        // ── Fecha actual ────────────────────────────────
        String fecha = new SimpleDateFormat("EEEE, dd 'de' MMMM",
                new Locale("es", "ES")).format(new Date());
        // Capitalizar primera letra
        fecha = fecha.substring(0, 1).toUpperCase() + fecha.substring(1);
        tvFecha.setText(fecha);

        // ── Materias reales del docente ─────────────────
        if (usuario != null) {
            List<Materia> materias = dbHelper.obtenerMateriasPorDocente(usuario.getId());

            if (materias.isEmpty()) {
                tvSinMaterias.setVisibility(View.VISIBLE);
            } else {
                for (Materia materia : materias) {
                    View card = crearTarjetaMateria(inflater, materia);
                    llMaterias.addView(card);
                }
                view.findViewById(R.id.cardGenerarQR).setOnClickListener(v ->
                        startActivity(new Intent(getActivity(), GenerarQRActivity.class)));
                view.findViewById(R.id.cardVerEstudiantes).setOnClickListener(v ->
                        mostrarEstudiantes(dbHelper));
            }
        }

        return view;
    }

    /** Genera el saludo según la hora del día */
    private String obtenerSaludo() {
        int hora = new Date().getHours();
        if (hora < 12) return "Buenos días";
        if (hora < 18) return "Buenas tardes";
        return "Buenas noches";
    }

    /** Crea una CardView dinámica con los datos de la materia */
    private View crearTarjetaMateria(LayoutInflater inflater, Materia materia) {
        // CardView
        CardView card = new CardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);
        card.setRadius(24f);
        card.setCardBackgroundColor(0xFF1A2A3A);
        card.setCardElevation(4f);

        // Layout interno
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(40, 32, 40, 32);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Info (nombre + codigo)
        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        info.setLayoutParams(infoParams);

        TextView tvNombre = new TextView(requireContext());
        tvNombre.setText(materia.getNombre());
        tvNombre.setTextColor(0xFFFFFFFF);
        tvNombre.setTextSize(15f);
        tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvCodigo = new TextView(requireContext());
        String subtitulo = (materia.getCodigo() != null && !materia.getCodigo().isEmpty())
                ? materia.getCodigo()
                : "Sin código";
        if (materia.getDescripcion() != null && !materia.getDescripcion().isEmpty()) {
            subtitulo += " • " + materia.getDescripcion();
        }
        tvCodigo.setText(subtitulo);
        tvCodigo.setTextColor(0xFF90CAF9);
        tvCodigo.setTextSize(12f);

        info.addView(tvNombre);
        info.addView(tvCodigo);

        row.addView(info);
        card.addView(row);

        return card;
    }
    private void mostrarEstudiantes(DatabaseHelper db) {
        List<Usuario> estudiantes = db.obtenerTodosLosEstudiantes();

        if (estudiantes.isEmpty()) {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Estudiantes")
                    .setMessage("No hay estudiantes registrados aún.")
                    .setPositiveButton("Cerrar", null)
                    .show();
            return;
        }

        // Construir lista con nombre + carnet
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < estudiantes.size(); i++) {
            Usuario u = estudiantes.get(i);
            sb.append(i + 1).append(". ")
                    .append(u.getNombre())
                    .append("\n   Carnet: ").append(u.getCarnet())
                    .append("\n   ").append(u.getCorreo());
            if (i < estudiantes.size() - 1) sb.append("\n\n");
        }

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("👥 Estudiantes (" + estudiantes.size() + ")")
                .setMessage(sb.toString())
                .setPositiveButton("Cerrar", null)
                .show();
    }
}