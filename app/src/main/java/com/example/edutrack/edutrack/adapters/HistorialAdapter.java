package com.example.edutrack.edutrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edutrack.R;
import com.example.edutrack.edutrack.models.Asistencia;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private final Context context;
    private final List<Asistencia> lista;

    public HistorialAdapter(Context context, List<Asistencia> lista) {
        this.context = context;
        this.lista   = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asistencia a = lista.get(position);
        int pct = a.getPorcentaje();

        holder.tvNombre.setText(a.getMateriaNombre());
        holder.tvFechaHora.setText(a.getFecha() + " • " + a.getHora());
        holder.tvPresentes.setText(a.getPresentes() + " / " + a.getTotal() + " presentes");
        holder.tvPorcentaje.setText(pct + "%");

        // Color del porcentaje según valor
        int color;
        if (pct >= 80)      color = Color.parseColor("#2E7D32");
        else if (pct >= 60) color = Color.parseColor("#F57F17");
        else                color = Color.parseColor("#B71C1C");

        holder.tvPorcentaje.getBackground().setTint(color);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFechaHora, tvPresentes, tvPorcentaje;
        ViewHolder(View v) {
            super(v);
            tvNombre     = v.findViewById(R.id.tvMateriaNombre);
            tvFechaHora  = v.findViewById(R.id.tvFechaHora);
            tvPresentes  = v.findViewById(R.id.tvPresentes);
            tvPorcentaje = v.findViewById(R.id.tvPorcentaje);
        }
    }
}