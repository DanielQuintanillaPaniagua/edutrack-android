package com.example.edutrack.edutrack.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.edutrack.R;
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

    public ReportesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reportes, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

        TextView tvPromedio    = view.findViewById(R.id.tvPromedioGeneral);
        TextView tvMaterias    = view.findViewById(R.id.tvTotalMaterias);
        TextView tvEstudiantes = view.findViewById(R.id.tvTotalEstudiantes);
        TextView tvAlertas     = view.findViewById(R.id.tvAlertas);
        BarChart barChart      = view.findViewById(R.id.barChart);

        // ── Datos reales del docente ─────────────────────
        Usuario usuario = dbHelper.obtenerUsuarioSesion();
        if (usuario == null) return view;

        List<Materia> materias = dbHelper.obtenerMateriasPorDocente(usuario.getId());
        int totalMaterias = materias.size();

        tvMaterias.setText(String.valueOf(totalMaterias));
        tvPromedio.setText("—");      // Se actualizará cuando haya asistencias
        tvEstudiantes.setText("0");   // Se actualizará cuando haya inscripciones
        tvAlertas.setText("0");

        // ── Gráfica de barras con MPAndroidChart ─────────
        configurarGrafica(barChart, materias);

        return view;
    }

    private void configurarGrafica(BarChart chart, List<Materia> materias) {
        if (materias.isEmpty()) {
            chart.setNoDataText("Agrega materias para ver la gráfica");
            chart.setNoDataTextColor(Color.parseColor("#90CAF9"));
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels   = new ArrayList<>();

        for (int i = 0; i < materias.size(); i++) {
            entries.add(new BarEntry(i, 0f)); // 0% hasta tener asistencias reales
            // Nombre corto para la etiqueta
            String nombre = materias.get(i).getNombre();
            labels.add(nombre.length() > 8 ? nombre.substring(0, 8) + "." : nombre);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Asistencia %");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        chart.setData(data);

        // Estilo del eje X
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#90CAF9"));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        // Estilo general
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