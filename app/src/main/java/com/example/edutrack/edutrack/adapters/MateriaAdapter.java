package com.example.edutrack.edutrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.models.Materia;

import java.util.List;

public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.ViewHolder> {

    public interface OnMateriaListener {
        void onEliminar(Materia materia, int position);
        void onClickMateria(Materia materia);
    }

    private final Context context;
    private final List<Materia> materias;
    private final OnMateriaListener listener;

    private final int[] COLORES = {
            Color.parseColor("#1565C0"),
            Color.parseColor("#2E7D32"),
            Color.parseColor("#6A1B9A"),
            Color.parseColor("#E65100"),
            Color.parseColor("#00695C"),
            Color.parseColor("#AD1457")
    };

    public MateriaAdapter(Context context, List<Materia> materias,
                          OnMateriaListener listener) {
        this.context  = context;
        this.materias = materias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_materia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Materia m = materias.get(position);

        // Círculo de color con la inicial
        String inicial = m.getNombre().substring(0, 1).toUpperCase();
        holder.tvInicial.setText(inicial);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(COLORES[position % COLORES.length]);
        holder.tvInicial.setBackground(circle);

        holder.tvNombre.setText(m.getNombre());
        holder.tvCodigo.setText(
                m.getCodigo() != null && !m.getCodigo().isEmpty()
                        ? m.getCodigo() : "Sin código");
        holder.tvDescripcion.setText(
                m.getDescripcion() != null && !m.getDescripcion().isEmpty()
                        ? m.getDescripcion() : "");

        holder.btnEliminar.setOnClickListener(v ->
                listener.onEliminar(m, holder.getAdapterPosition()));

        holder.itemView.setOnClickListener(v -> listener.onClickMateria(m));
    }

    @Override
    public int getItemCount() { return materias.size(); }

    public void agregar(Materia m) {
        materias.add(m);
        notifyItemInserted(materias.size() - 1);
    }

    public void eliminar(int position) {
        materias.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInicial, tvNombre, tvCodigo, tvDescripcion, btnEliminar;

        ViewHolder(View v) {
            super(v);
            tvInicial    = v.findViewById(R.id.tvInicialMateria);
            tvNombre     = v.findViewById(R.id.tvNombreMateria);
            tvCodigo     = v.findViewById(R.id.tvCodigoMateria);
            tvDescripcion= v.findViewById(R.id.tvDescripcionMateria);
            btnEliminar  = v.findViewById(R.id.btnEliminarMateria);
        }
    }
}