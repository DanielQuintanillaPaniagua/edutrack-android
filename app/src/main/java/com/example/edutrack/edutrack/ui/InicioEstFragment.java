package com.example.edutrack.edutrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;

import java.util.List;

public class InicioEstFragment extends Fragment {

    private static final int UMBRAL = 80;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_inicio_est_fragment, container, false);

        DatabaseHelper db   = new DatabaseHelper(requireContext());
        Usuario usuario      = db.obtenerUsuarioSesion();
        if (usuario == null) return view;

        // Saludo
        ((TextView) view.findViewById(R.id.tvSaludoEst))
                .setText("Hola, " + usuario.getNombre() + " 👋");

        // Resumen
        List<Object[]> resumen = db.obtenerResumenAsistenciaEstudiante(usuario.getId());

        int totalAsistidas = 0, totalClases = 0;
        for (Object[] fila : resumen) {
            totalAsistidas += (int) fila[2];
            totalClases    += (int) fila[3];
        }

        int pct = totalClases > 0 ? (int)((totalAsistidas * 100.0f) / totalClases) : 0;

        TextView tvPromedio = view.findViewById(R.id.tvPromedioEst);
        tvPromedio.setText(totalClases > 0 ? pct + "%" : "—");
        tvPromedio.setTextColor(requireContext().getColor(
                pct >= UMBRAL ? R.color.green_primary : android.R.color.holo_red_dark));

        ((ProgressBar) view.findViewById(R.id.progressAsistencia)).setProgress(pct);
        ((TextView) view.findViewById(R.id.tvClasesAsistidas)).setText(String.valueOf(totalAsistidas));
        ((TextView) view.findViewById(R.id.tvClasesTotales)).setText(String.valueOf(totalClases));

        // Alertas dinámicas
        LinearLayout container2   = view.findViewById(R.id.layoutAlertasContainer);
        TextView tvSinAlertas     = view.findViewById(R.id.tvSinAlertas);
        TextView tvVerTodas       = view.findViewById(R.id.tvVerTodasAlertas);

        int enRiesgo = 0;
        for (Object[] fila : resumen) {
            int p = (int) fila[1];
            if (p >= 0 && p < UMBRAL) {
                enRiesgo++;
                Materia m = (Materia) fila[0];
                agregarAlerta(inflater, container2,
                        "⚠️", "#D32F2F",
                        "Riesgo en " + m.getNombre(),
                        "Tu asistencia es " + p + "% — mínimo requerido " + UMBRAL + "%");
            }
        }

        if (enRiesgo == 0) {
            tvSinAlertas.setVisibility(View.VISIBLE);
        }

        // "Ver todas" navega al fragment de alertas
        tvVerTodas.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorEstudiante, new AlertasFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void agregarAlerta(LayoutInflater inflater, LinearLayout container,
                               String icono, String colorHex,
                               String titulo, String descripcion) {
        View item = inflater.inflate(R.layout.item_alerta_inicio, container, false);
        ((TextView) item.findViewById(R.id.tvAlertaIcoInicio)).setText(icono);
        ((TextView) item.findViewById(R.id.tvAlertaTituloInicio)).setText(titulo);
        ((TextView) item.findViewById(R.id.tvAlertaTituloInicio))
                .setTextColor(android.graphics.Color.parseColor(colorHex));
        ((TextView) item.findViewById(R.id.tvAlertaDescInicio)).setText(descripcion);
        container.addView(item);
    }
}