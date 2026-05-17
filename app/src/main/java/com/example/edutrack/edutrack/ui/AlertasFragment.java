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

import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;

import java.util.ArrayList;
import java.util.List;

public class AlertasFragment extends Fragment {

    private static final int UMBRAL_RIESGO = 80;

    // ── Adapter ─────────────────────────────────────────
    static class AlertasAdapter extends RecyclerView.Adapter<AlertasAdapter.VH> {

        private final List<Object[]> lista;

        AlertasAdapter(List<Object[]> lista) {
            this.lista = lista;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_alerta, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Object[] fila     = lista.get(pos);
            Materia materia   = (Materia) fila[0];
            int porcentaje    = (int) fila[1];
            int asistidas     = (int) fila[2];
            int total         = (int) fila[3];

            h.tvTitulo.setText("Asistencia baja en " + materia.getNombre());
            h.tvDescripcion.setText(
                    "Tu asistencia actual es " + porcentaje + "%.\n" +
                            "El mínimo requerido es " + UMBRAL_RIESGO + "%.");
            h.tvDetalle.setText(asistidas + " de " + total + " clases asistidas");
        }

        @Override public int getItemCount() { return lista.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitulo, tvDescripcion, tvDetalle;
            VH(@NonNull View v) {
                super(v);
                tvTitulo      = v.findViewById(R.id.tvAlertaTitulo);
                tvDescripcion = v.findViewById(R.id.tvAlertaDescripcion);
                tvDetalle     = v.findViewById(R.id.tvAlertaDetalle);
            }
        }
    }

    // ── Fragment ─────────────────────────────────────────
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_alertas_fragment, container, false);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        Usuario usuario   = db.obtenerUsuarioSesion();

        if (usuario == null) return view;

        List<Object[]> resumen   = db.obtenerResumenAsistenciaEstudiante(usuario.getId());
        List<Object[]> enRiesgo  = new ArrayList<>();

        for (Object[] fila : resumen) {
            int pct = (int) fila[1];
            if (pct >= 0 && pct < UMBRAL_RIESGO) enRiesgo.add(fila);
        }

        TextView tvSinAlertas = view.findViewById(R.id.tvSinAlertas);
        RecyclerView rvAlertas = view.findViewById(R.id.rvAlertas);

        if (enRiesgo.isEmpty()) {
            tvSinAlertas.setVisibility(View.VISIBLE);
            rvAlertas.setVisibility(View.GONE);
        } else {
            tvSinAlertas.setVisibility(View.GONE);
            rvAlertas.setVisibility(View.VISIBLE);
            rvAlertas.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvAlertas.setAdapter(new AlertasAdapter(enRiesgo));
        }

        return view;
    }
}