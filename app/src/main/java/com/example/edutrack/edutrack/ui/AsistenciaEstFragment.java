package com.example.edutrack.edutrack.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Usuario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AsistenciaEstFragment extends Fragment {

    // ── Modelo ──────────────────────────────────────────
    public static class RegistroAsistencia {
        public final String nombreMateria;
        public final String hora;
        public final String estado;

        public RegistroAsistencia(String nombre, String hora, String estado) {
            this.nombreMateria = nombre;
            this.hora          = hora;
            this.estado        = estado;
        }
    }

    // ── Adapter ─────────────────────────────────────────
    static class AsistenciaDiaAdapter
            extends RecyclerView.Adapter<AsistenciaDiaAdapter.VH> {

        private List<RegistroAsistencia> lista = new ArrayList<>();

        void actualizar(List<RegistroAsistencia> nueva) {
            lista = nueva;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_asistencia_dia, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            RegistroAsistencia r = lista.get(pos);
            h.tvNombre.setText(r.nombreMateria);
            h.tvHora.setText("Hora: " + r.hora);
            boolean ok = "presente".equalsIgnoreCase(r.estado);
            h.tvIcono.setText(ok ? "✅" : "❌");
            h.tvEstado.setText(ok ? "Presente" : "Ausente");
            h.tvEstado.setTextColor(h.itemView.getContext().getColor(
                    ok ? android.R.color.holo_green_dark
                            : android.R.color.holo_red_dark));
        }

        @Override public int getItemCount() { return lista.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvIcono, tvNombre, tvHora, tvEstado;
            VH(@NonNull View v) {
                super(v);
                tvIcono  = v.findViewById(R.id.tvIconoEstado);
                tvNombre = v.findViewById(R.id.tvNombreMateria);
                tvHora   = v.findViewById(R.id.tvHoraRegistro);
                tvEstado = v.findViewById(R.id.tvEstadoLabel);
            }
        }
    }

    // ── Fragment ─────────────────────────────────────────
    private DatabaseHelper db;
    private int estudianteId;

    // {nombre_materia, hora, fecha, estado}
    private final List<String[]> historialRaw = new ArrayList<>();

    private CalendarView calendarAsistencia;
    private RecyclerView rvAsistenciaDia;
    private TextView tvFechaSeleccionada;
    private AsistenciaDiaAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_asistencia_est_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());
        Usuario sesion = db.obtenerUsuarioSesion();
        if (sesion == null) return;
        estudianteId = sesion.getId();

        // Bind — IDs de TU layout
        calendarAsistencia  = view.findViewById(R.id.calendarAsistencia);
        rvAsistenciaDia     = view.findViewById(R.id.rvAsistenciaDia);
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada);

        adapter = new AsistenciaDiaAdapter();
        rvAsistenciaDia.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAsistenciaDia.setAdapter(adapter);

        // Cargar todos los registros
        cargarHistorial();

        // Mostrar hoy por defecto
        String hoy = hoy();
        tvFechaSeleccionada.setText("📅 " + hoy);
        filtrarPorFecha(hoy);

        // Cambio de fecha en calendario
        calendarAsistencia.setOnDateChangeListener((cal, year, month, day) -> {
            // month es 0-based en CalendarView
            String fecha = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d", year, month + 1, day);
            tvFechaSeleccionada.setText("📅 " + fecha);
            filtrarPorFecha(fecha);
        });
    }

    private void cargarHistorial() {
        historialRaw.clear();

        Cursor c = db.obtenerHistorialEstudiante(estudianteId);

        if (c != null) {
            while (c.moveToNext()) {
                historialRaw.add(new String[]{
                        c.getString(c.getColumnIndexOrThrow("materia_nombre")), // ← aquí
                        c.getString(c.getColumnIndexOrThrow("hora")),
                        c.getString(c.getColumnIndexOrThrow("fecha")),
                        c.getString(c.getColumnIndexOrThrow("estado"))
                });
            }
            c.close();
        }
    }
    private void filtrarPorFecha(String fecha) {
        List<RegistroAsistencia> filtrados = new ArrayList<>();
        for (String[] r : historialRaw)
            if (fecha.equals(r[2]))
                filtrados.add(new RegistroAsistencia(r[0], r[1], r[3]));

        adapter.actualizar(filtrados);

        // Si no hay registros, el RecyclerView queda vacío —
        // puedes agregar un tvVacio a tu layout si quieres mostrarlo
        tvFechaSeleccionada.setText(filtrados.isEmpty()
                ? "📅 " + fecha + " — sin registros"
                : "📅 " + fecha);
    }

    private String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}