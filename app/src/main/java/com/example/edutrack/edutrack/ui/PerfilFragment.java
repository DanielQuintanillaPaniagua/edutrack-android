package com.example.edutrack.edutrack.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.edutrack.edutrack.R;

import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilFragment extends Fragment {

    private CircleImageView imgPerfil;
    private SharedPreferences prefs;
    private static final String KEY_FOTO = "foto_perfil_uri";

    // Launcher para galería
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            String rutaLocal = copiarFotoAlStorage(uri);
                            if (rutaLocal != null) {
                                imgPerfil.setImageBitmap(
                                        android.graphics.BitmapFactory.decodeFile(rutaLocal));
                                prefs.edit().putString(KEY_FOTO, rutaLocal).apply();
                            }
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        prefs = requireActivity()
                .getSharedPreferences("edutrack_prefs", 0);

        // ── Vistas ──────────────────────────────────────
        imgPerfil          = view.findViewById(R.id.imgPerfil);
        TextView tvNombre  = view.findViewById(R.id.tvNombrePerfil);
        TextView tvRol     = view.findViewById(R.id.tvRolPerfil);
        TextView tvCorreo  = view.findViewById(R.id.tvCorreoPerfil);

        TextView tvInfoPersonal    = view.findViewById(R.id.tvInfoPersonal);
        TextView tvCambiarPassword = view.findViewById(R.id.tvCambiarPassword);
        TextView tvNotificaciones  = view.findViewById(R.id.tvNotificaciones);
        TextView tvPreferencias    = view.findViewById(R.id.tvPreferencias);
        TextView tvAyuda           = view.findViewById(R.id.tvAyuda);
        Button   btnCerrarSesion   = view.findViewById(R.id.btnCerrarSesion);

        // ── Datos reales desde sesión ────────────────────
        Usuario usuario = dbHelper.obtenerUsuarioSesion();
        if (usuario != null) {
            tvNombre.setText(usuario.getNombre());
            String rol = usuario.getRol();
            tvRol.setText(rol.substring(0, 1).toUpperCase() + rol.substring(1));
            tvCorreo.setText(usuario.getCorreo());
        }

        // ── Foto guardada ────────────────────────────────
        String rutaFoto = prefs.getString(KEY_FOTO, null);
        if (rutaFoto != null) {
            File fotoFile = new File(rutaFoto);
            if (fotoFile.exists()) {
                imgPerfil.setImageBitmap(
                        android.graphics.BitmapFactory.decodeFile(rutaFoto));
            }
        }

        // ── Click en foto → galería ──────────────────────
        imgPerfil.setOnClickListener(v -> pickImage.launch("image/*"));

        // ── Opciones del menú ────────────────────────────
        tvInfoPersonal.setOnClickListener(v ->
                mostrarInfoPersonal(usuario));

        tvCambiarPassword.setOnClickListener(v ->
                mostrarCambiarPassword(dbHelper, usuario));

        tvNotificaciones.setOnClickListener(v ->
                Toast.makeText(getContext(), "🔔 Próximamente",
                        Toast.LENGTH_SHORT).show());

        tvPreferencias.setOnClickListener(v ->
                Toast.makeText(getContext(), "⚙️ Próximamente",
                        Toast.LENGTH_SHORT).show());

        tvAyuda.setOnClickListener(v ->
                mostrarAyuda());

        // ── Cerrar sesión ────────────────────────────────
        btnCerrarSesion.setOnClickListener(v ->
                new AlertDialog.Builder(getContext())
                        .setTitle("Cerrar sesión")
                        .setMessage("¿Estás seguro que deseas cerrar sesión?")
                        .setPositiveButton("Sí, salir", (d, w) -> {
                            dbHelper.cerrarSesion();
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show());

        return view;
    }

    // ── Diálogo: Información personal ───────────────────
    private void mostrarInfoPersonal(Usuario u) {
        if (u == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Información personal")
                .setMessage(
                        "👤 Nombre: " + u.getNombre() + "\n" +
                                "📧 Correo: " + u.getCorreo() + "\n" +
                                "🎓 Carnet: " + (u.getCarnet() != null ? u.getCarnet() : "—") + "\n" +
                                "🏷️ Rol: " + u.getRol())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // ── Diálogo: Cambiar contraseña ──────────────────────
    private void mostrarCambiarPassword(DatabaseHelper dbHelper, Usuario u) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        EditText etActual = new EditText(getContext());
        etActual.setHint("Contraseña actual");
        etActual.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etActual);

        EditText etNueva = new EditText(getContext());
        etNueva.setHint("Nueva contraseña");
        etNueva.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNueva);

        EditText etConfirmar = new EditText(getContext());
        etConfirmar.setHint("Confirmar nueva contraseña");
        etConfirmar.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirmar);

        new AlertDialog.Builder(getContext())
                .setTitle("Cambiar contraseña")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    String actual    = etActual.getText().toString().trim();
                    String nueva     = etNueva.getText().toString().trim();
                    String confirmar = etConfirmar.getText().toString().trim();

                    if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
                        Toast.makeText(getContext(), "Completa todos los campos",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!nueva.equals(confirmar)) {
                        Toast.makeText(getContext(), "Las contraseñas no coinciden",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nueva.length() < 6) {
                        Toast.makeText(getContext(), "Mínimo 6 caracteres",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Verificar contraseña actual
                    if (!dbHelper.verificarCredenciales(u.getCorreo(), actual)) {
                        Toast.makeText(getContext(), "Contraseña actual incorrecta",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbHelper.cambiarPassword(u.getCorreo(), nueva);
                    Toast.makeText(getContext(), "✅ Contraseña actualizada",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ── Diálogo: Ayuda ───────────────────────────────────
    private void mostrarAyuda() {
        new AlertDialog.Builder(getContext())
                .setTitle("❓ Ayuda y soporte")
                .setMessage(
                        "EduTrack v1.0\n\n" +
                                "🔔 Próximamente")
                .setPositiveButton("Cerrar", null)
                .show();
    }
    private String copiarFotoAlStorage(Uri uri) {
        try {
            InputStream input = requireContext()
                    .getContentResolver().openInputStream(uri);
            File destino = new File(requireContext().getFilesDir(), "foto_perfil.jpg");
            OutputStream output = new FileOutputStream(destino);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            input.close();
            output.close();

            return destino.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}