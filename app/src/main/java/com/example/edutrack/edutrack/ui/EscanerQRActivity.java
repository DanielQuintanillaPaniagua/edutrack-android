package com.example.edutrack.edutrack.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.edutrack.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.database.FirebaseManager;
import com.example.edutrack.edutrack.models.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.google.firebase.firestore.FieldValue;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EscanerQRActivity extends AppCompatActivity {

    private static final int PERMISO_CAMARA = 100;
    DecoratedBarcodeView barcodeScanner;
    DatabaseHelper db;
    boolean yaProcesado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escaner_qr);

        db = new DatabaseHelper(this);
        barcodeScanner = findViewById(R.id.barcodeScanner);

        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
        } else {
            iniciarEscaner();
        }
    }
    // Añadir este método nuevo a EscanerQRActivity
    private void actualizarConteoEnFirestore(int materiaId, String fecha) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Contar inscritos localmente (el estudiante los tiene sincronizados)
        int totalInscritos = db.contarInscritosPorMateria(materiaId);

        firestore.collection("asistencia")
                .whereEqualTo("materia_id", materiaId)
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        // El documento ya existe → solo incrementar presentes
                        query.getDocuments().get(0).getReference()
                                .update(
                                        "presentes", FieldValue.increment(1),
                                        "total", totalInscritos
                                );
                    } else {
                        // No existe aún → crearlo (edge case: docente generó QR sin conexión)
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("materia_id", materiaId);
                        doc.put("fecha", fecha);
                        doc.put("presentes", 1);
                        doc.put("total", totalInscritos);
                        firestore.collection("asistencia").add(doc);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_CAMARA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarEscaner();
        } else {
            Toast.makeText(this, "Se necesita permiso de cámara",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void iniciarEscaner() {
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (yaProcesado) return;
                yaProcesado = true;
                procesarQR(result.getText());
            }
        });
    }

    private void procesarQR(String contenido) {
        if (!contenido.startsWith("edutrack://asistencia/")) {
            Toast.makeText(this, "QR inválido", Toast.LENGTH_SHORT).show();
            yaProcesado = false;
            return;
        }

        try {
            String[] partes = contenido
                    .replace("edutrack://asistencia/", "").split("/");

            int materiaId        = Integer.parseInt(partes[0]);
            String materiaNombre = partes[1].replace("_", " ");
            String fechaQR       = partes[2];

            String fechaHoy  = new SimpleDateFormat("yyyy-MM-dd",
                    new Locale("es", "ES")).format(new Date());
            String horaAhora = new SimpleDateFormat("HH:mm",
                    new Locale("es", "ES")).format(new Date());

            if (!fechaQR.equals(fechaHoy)) {
                Toast.makeText(this, "QR expirado — fecha: " + fechaQR +
                        " hoy: " + fechaHoy, Toast.LENGTH_LONG).show();
                yaProcesado = false;
                return;
            }

            Usuario estudiante = db.obtenerUsuarioSesion();
            if (estudiante == null) {
                Toast.makeText(this, "Error: no hay sesión activa",
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            boolean registrado = db.registrarAsistenciaEstudiante(
                    estudiante.getId(), materiaId, fechaHoy, horaAhora);

            if (registrado) {
                db.actualizarConteoAsistencia(materiaId, fechaHoy);

                FirebaseManager.sincronizarAsistenciaEstudiante(
                        estudiante.getId(), materiaId, fechaHoy, horaAhora, "presente");

                // ✅ NUEVO: actualizar conteo agregado en Firestore
                actualizarConteoEnFirestore(materiaId, fechaHoy);

                Toast.makeText(this,
                        "✅ Asistencia registrada en " + materiaNombre,
                        Toast.LENGTH_LONG).show();
            }

            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            yaProcesado = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }
}