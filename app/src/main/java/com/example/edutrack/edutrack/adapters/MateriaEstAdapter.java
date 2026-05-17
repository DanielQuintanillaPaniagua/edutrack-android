package com.example.edutrack.edutrack.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.models.Materia;

import java.util.List;

public class MateriaEstAdapter extends RecyclerView.Adapter<MateriaEstAdapter.ViewHolder> {

    private List<Object[]> lista;

    public MateriaEstAdapter(List<Object[]> lista) {
        this.lista = lista;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia_est, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object[] fila = lista.get(position);
        Materia materia      = (Materia) fila[0];
        int porcentaje       = (int)    fila[1];
        int clasesAsistidas  = (int)    fila[2];
        int totalClases      = (int)    fila[3];

        holder.tvNombre.setText(materia.getNombre());
        holder.tvClasesDetalle.setText(clasesAsistidas + "/" + totalClases + " clases");

        if (porcentaje >= 0) {
            holder.tvPorcentaje.setText(porcentaje + "%");
            holder.progress.setProgress(porcentaje);

            // Color según umbral
            String color;
            if      (porcentaje >= 80) color = "#4CAF50"; // verde
            else if (porcentaje >= 60) color = "#FFC107"; // amarillo
            else                       color = "#F44336"; // rojo

            holder.tvPorcentaje.setTextColor(Color.parseColor(color));
            holder.progress.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor(color)));
        } else {
            holder.tvPorcentaje.setText("—");
            holder.progress.setProgress(0);
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPorcentaje, tvClasesDetalle;
        ProgressBar progress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre       = itemView.findViewById(R.id.tvNombreMateria);
            tvPorcentaje   = itemView.findViewById(R.id.tvPorcentaje);
            tvClasesDetalle= itemView.findViewById(R.id.tvClasesDetalle);
            progress       = itemView.findViewById(R.id.progressAsistencia);
        }
    }
}