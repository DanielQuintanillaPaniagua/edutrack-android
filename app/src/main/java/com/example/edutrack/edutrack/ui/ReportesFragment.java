package com.example.edutrack.edutrack.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class ReportesFragment extends Fragment {

    private static final int UMBRAL = 80;

    public ReportesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reportes, container, false);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        Usuario usuario   = db.obtenerUsuarioSesion();
        if (usuario == null) return view;

        TextView tvPromedio    = view.findViewById(R.id.tvPromedioGeneral);
        TextView tvMaterias    = view.findViewById(R.id.tvTotalMaterias);
        TextView tvEstudiantes = view.findViewById(R.id.tvTotalEstudiantes);
        TextView tvAlertas     = view.findViewById(R.id.tvAlertas);
        BarChart barChart      = view.findViewById(R.id.barChart);

        List<Materia> materias = db.obtenerMateriasPorDocente(usuario.getId());
        tvMaterias.setText(String.valueOf(materias.size()));

        // ── Promedio general ────────────────────────────
        int totalPresentes = 0, totalClases = 0;
        for (Materia m : materias) {
            int[] datos = obtenerSumaAsistencia(db, m.getId());
            totalPresentes += datos[0];
            totalClases    += datos[1];
        }
        if (totalClases > 0) {
            int pct = (int)((totalPresentes * 100.0f) / totalClases);
            tvPromedio.setText(pct + "%");
        } else {
            tvPromedio.setText("—");
        }

        // ── Total estudiantes inscritos (distintos) ─────
        int totalEstudiantes = obtenerTotalEstudiantes(db, usuario.getId());
        tvEstudiantes.setText(String.valueOf(totalEstudiantes));

        // ── Alertas activas (estudiantes < 80%) ─────────
        int alertas = obtenerAlertasActivas(db, materias);
        tvAlertas.setText(String.valueOf(alertas));

        // ── Gráfica con % reales ─────────────────────────
        configurarGrafica(barChart, db, materias);

        return view;
    }

    /** SUM(presentes) y SUM(total) de una materia en la tabla asistencia */
    private int[] obtenerSumaAsistencia(DatabaseHelper db, int materiaId) {
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(presentes),0), COALESCE(SUM(total),0) " +
                        "FROM asistencia WHERE materia_id = ?",
                new String[]{String.valueOf(materiaId)});
        int[] res = {0, 0};
        if (c.moveToFirst()) {
            res[0] = c.getInt(0); // presentes
            res[1] = c.getInt(1); // total
        }
        c.close();
        return res;
    }

    /** Estudiantes únicos inscritos en todas las materias del docente */
    private int obtenerTotalEstudiantes(DatabaseHelper db, int docenteId) {
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(DISTINCT i.estudiante_id) " +
                        "FROM inscripciones i " +
                        "INNER JOIN materias m ON i.materia_id = m.id " +
                        "WHERE m.docente_id = ?",
                new String[]{String.valueOf(docenteId)});
        int total = 0;
        if (c.moveToFirst()) total = c.getInt(0);
        c.close();
        return total;
    }

    /**
     * Cuenta cuántos estudiantes tienen < 80% en al menos una materia del docente.
     * Usa asistencia_estudiante para el numerador y asistencia para el denominador.
     */
    private int obtenerAlertasActivas(DatabaseHelper db, List<Materia> materias) {
        int alertas = 0;
        for (Materia m : materias) {
            // Estudiantes inscritos en esta materia
            Cursor est = db.getReadableDatabase().rawQuery(
                    "SELECT estudiante_id FROM inscripciones WHERE materia_id = ?",
                    new String[]{String.valueOf(m.getId())});

            // Total de sesiones del docente en esta materia
            int[] suma = obtenerSumaAsistencia(db, m.getId());
            int totalSesiones = suma[1];
            if (totalSesiones == 0) { est.close(); continue; }

            while (est.moveToNext()) {
                int estId = est.getInt(0);
                Cursor pres = db.getReadableDatabase().rawQuery(
                        "SELECT COUNT(*) FROM asistencia_estudiante " +
                                "WHERE estudiante_id=? AND materia_id=? AND estado='presente'",
                        new String[]{String.valueOf(estId), String.valueOf(m.getId())});
                int presentes = 0;
                if (pres.moveToFirst()) presentes = pres.getInt(0);
                pres.close();

                int pct = (int)((presentes * 100.0f) / totalSesiones);
                if (pct < UMBRAL) alertas++;
            }
            est.close();
        }
        return alertas;
    }

    /** Gráfica de barras con % real por materia */
    private void configurarGrafica(BarChart chart, DatabaseHelper db,
                                   List<Materia> materias) {
        if (materias.isEmpty()) {
            chart.setNoDataText("Agrega materias para ver la gráfica");
            chart.setNoDataTextColor(Color.parseColor("#90CAF9"));
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels   = new ArrayList<>();

        for (int i = 0; i < materias.size(); i++) {
            Materia m    = materias.get(i);
            int[] datos  = obtenerSumaAsistencia(db, m.getId());
            float pct    = datos[1] > 0 ? (datos[0] * 100.0f) / datos[1] : 0f;

            entries.add(new BarEntry(i, pct));
            String nombre = m.getNombre();
            labels.add(nombre.length() > 8 ? nombre.substring(0, 8) + "." : nombre);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Asistencia %");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        chart.setData(data);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#90CAF9"));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        chart.getAxisLeft().setTextColor(Color.parseColor("#90CAF9"));
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.parseColor("#1A2A3A"));
        chart.setFitBars(true);
        chart.animateY(800);
        chart.invalidate();
    }
}