package com.example.edutrack.edutrack.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilEstFragment extends Fragment {

    private DatabaseHelper db;
    private Usuario usuario;
    private CircleImageView imgPerfil;
    private SharedPreferences prefs;
    private static final String KEY_FOTO_EST = "foto_perfil_est_uri";


    // Launcher para abrir galería
    private final ActivityResultLauncher<String> galeriaLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            String rutaLocal = copiarFotoAlStorage(uri);
                            if (rutaLocal != null) {
                                imgPerfil.setImageBitmap(
                                        android.graphics.BitmapFactory.decodeFile(rutaLocal));
                                prefs.edit().putString(KEY_FOTO_EST, rutaLocal).apply();
                            }
                        }
                    });
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_perfil_est_fragment, container, false);

        db      = new DatabaseHelper(requireContext());
        usuario = db.obtenerUsuarioSesion();
        prefs = requireActivity().getSharedPreferences("edutrack_prefs", 0);


        // Bind views
        imgPerfil = view.findViewById(R.id.imgPerfilEst);
        String rutaFoto = prefs.getString(KEY_FOTO_EST, null);
        if (rutaFoto != null) {
            File fotoFile = new File(rutaFoto);
            if (fotoFile.exists()) {
                imgPerfil.setImageBitmap(
                        android.graphics.BitmapFactory.decodeFile(rutaFoto));
            }
        }
        if (usuario != null) {
            ((TextView) view.findViewById(R.id.tvNombreEst)).setText(usuario.getNombre());
            ((TextView) view.findViewById(R.id.tvCorreoEst)).setText(usuario.getCorreo());
            ((TextView) view.findViewById(R.id.tvCarnetEst)).setText("Código: " + usuario.getCarnet());
        }

        // Foto desde galería
        imgPerfil.setOnClickListener(v ->
                galeriaLauncher.launch("image/*"));

        // Información personal
        view.findViewById(R.id.layoutInfoPersonal).setOnClickListener(v ->
                mostrarInfoPersonal());

        // Cambiar contraseña
        view.findViewById(R.id.layoutCambiarPassword).setOnClickListener(v ->
                mostrarCambiarPassword());

        // Próximamente
        view.findViewById(R.id.layoutNotificaciones).setOnClickListener(v ->
                proximamente());
        view.findViewById(R.id.layoutAyuda).setOnClickListener(v ->
                proximamente());

        // Cerrar sesión
        view.findViewById(R.id.btnCerrarSesionEst).setOnClickListener(v -> {
            db.cerrarSesion();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void mostrarInfoPersonal() {
        if (usuario == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Información personal")
                .setMessage(
                        "Nombre: "  + usuario.getNombre()  + "\n\n" +
                                "Correo: "  + usuario.getCorreo()  + "\n\n" +
                                "Código: "  + usuario.getCarnet()  + "\n\n" +
                                "Rol: "     + usuario.getRol())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarCambiarPassword() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_cambiar_password, null);

        EditText etActual   = dialogView.findViewById(R.id.etPasswordActual);
        EditText etNueva    = dialogView.findViewById(R.id.etPasswordNueva);
        EditText etConfirma = dialogView.findViewById(R.id.etPasswordConfirma);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar contraseña")
                .setView(dialogView)
                .setPositiveButton("Guardar", (d, w) -> {
                    String actual   = etActual.getText().toString().trim();
                    String nueva    = etNueva.getText().toString().trim();
                    String confirma = etConfirma.getText().toString().trim();

                    if (actual.isEmpty() || nueva.isEmpty() || confirma.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!nueva.equals(confirma)) {
                        Toast.makeText(requireContext(),
                                "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Verificar que la actual sea correcta
                    if (!db.verificarCredenciales(usuario.getCorreo(), actual)) {
                        Toast.makeText(requireContext(),
                                "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.cambiarPassword(usuario.getCorreo(), nueva);
                    Toast.makeText(requireContext(),
                            "✅ Contraseña actualizada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void proximamente() {
        Toast.makeText(requireContext(),
                "🚧 En desarrollo — próximamente", Toast.LENGTH_SHORT).show();
    }
    private String copiarFotoAlStorage(Uri uri) {
        try {
            InputStream input = requireContext()
                    .getContentResolver().openInputStream(uri);
            File destino = new File(requireContext().getFilesDir(), "foto_perfil_est.jpg");
            OutputStream output = new FileOutputStream(destino);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            input.close();
            output.close();

            return destino.getAbsolutePath(); // ← ruta interna permanente
        } catch (Exception e) {
            return null;
        }
    }
}
