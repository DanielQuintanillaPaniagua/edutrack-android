package com.example.edutrack.edutrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.database.Cursor;

import androidx.fragment.app.Fragment;

import com.example.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InicioFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        TextView tvSaludo = view.findViewById(R.id.tvSaludo);
        TextView tvFecha = view.findViewById(R.id.tvFecha);

        // Mostrar nombre del usuario desde SQLite
        Cursor cursor = dbHelper.obtenerSesion();
        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            tvSaludo.setText("Buenos días, " + nombre);
        }
        cursor.close();

        // Mostrar fecha actual
        String fecha = new SimpleDateFormat("EEEE, dd 'de' MMMM",
                new Locale("es", "ES")).format(new Date());
        tvFecha.setText(fecha);

        return view;
    }
}