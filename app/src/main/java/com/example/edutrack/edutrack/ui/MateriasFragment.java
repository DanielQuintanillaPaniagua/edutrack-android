package com.example.edutrack.edutrack.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edutrack.R;
import com.example.edutrack.edutrack.adapters.MateriaAdapter;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;

import java.util.List;

public class MateriasFragment extends Fragment implements MateriaAdapter.OnMateriaListener {

    private MateriaAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Materia> listaMaterias;
    private int docenteId;

    public MateriasFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materias, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Obtener docente activo
        Usuario usuario = dbHelper.obtenerUsuarioSesion();
        if (usuario != null) docenteId = usuario.getId();

        // RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvMaterias);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        listaMaterias = dbHelper.obtenerMateriasPorDocente(docenteId);
        adapter = new MateriaAdapter(getContext(), listaMaterias, this);
        rv.setAdapter(adapter);

        // Botón nueva materia
        Button btnNueva = view.findViewById(R.id.btnNuevaMateria);
        btnNueva.setOnClickListener(v -> mostrarDialogo());

        return view;
    }

    private void mostrarDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nueva Materia");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        EditText etNombre = new EditText(getContext());
        etNombre.setHint("Nombre *");
        layout.addView(etNombre);

        EditText etCodigo = new EditText(getContext());
        etCodigo.setHint("Código (ej: MAT-101)");
        layout.addView(etCodigo);

        EditText etDescripcion = new EditText(getContext());
        etDescripcion.setHint("Descripción");
        layout.addView(etDescripcion);

        builder.setView(layout);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(getContext(), "El nombre es requerido",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Materia nueva = new Materia(
                    nombre,
                    etCodigo.getText().toString().trim(),
                    etDescripcion.getText().toString().trim(),
                    docenteId);

            long id = dbHelper.agregarMateria(nueva);
            if (id > 0) {
                nueva.setId((int) id);
                adapter.agregar(nueva);
                Toast.makeText(getContext(), "✅ Materia agregada",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public void onEliminar(Materia materia, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar materia")
                .setMessage("¿Eliminar \"" + materia.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    if (dbHelper.eliminarMateria(materia.getId())) {
                        adapter.eliminar(position);
                        Toast.makeText(getContext(), "Materia eliminada",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onClickMateria(Materia materia) {
        Toast.makeText(getContext(), materia.getNombre(), Toast.LENGTH_SHORT).show();
    }
}