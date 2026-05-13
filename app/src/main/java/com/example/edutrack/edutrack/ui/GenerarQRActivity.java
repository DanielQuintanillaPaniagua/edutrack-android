package com.example.edutrack.edutrack.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.edutrack.R;
import com.example.edutrack.edutrack.database.DatabaseHelper;
import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerarQRActivity extends AppCompatActivity {

    private Spinner spinnerMaterias;
    private ImageView imgQR;
    private CardView cardQR;
    private TextView tvInfoSesion, tvTemporizador;
    private Button btnGenerarQR;

    private List<Materia> listaMaterias = new ArrayList<>();
    private Materia materiaSeleccionada;
    private CountDownTimer timer;
    private DatabaseHelper dbHelper;

    // QR válido por 5 minutos
    private static final long QR_DURACION_MS = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_qr);

        dbHelper       = new DatabaseHelper(this);
        spinnerMaterias = findViewById(R.id.spinnerMaterias);
        imgQR          = findViewById(R.id.imgQR);
        cardQR         = findViewById(R.id.cardQR);
        tvInfoSesion   = findViewById(R.id.tvInfoSesion);
        tvTemporizador = findViewById(R.id.tvTemporizador);
        btnGenerarQR   = findViewById(R.id.btnGenerarQR);

        cargarMaterias();

        btnGenerarQR.setOnClickListener(v -> generarQR());

        findViewById(R.id.btnCerrar).setOnClickListener(v -> finish());
    }

    private void cargarMaterias() {
        Usuario usuario = dbHelper.obtenerUsuarioSesion();
        if (usuario == null) {
            Toast.makeText(this, "Error: sin sesión activa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listaMaterias = dbHelper.obtenerMateriasPorDocente(usuario.getId());

        if (listaMaterias.isEmpty()) {
            Toast.makeText(this,
                    "No tienes materias registradas", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        List<String> nombres = new ArrayList<>();
        for (Materia m : listaMaterias) {
            String label = m.getNombre();
            if (m.getCodigo() != null && !m.getCodigo().isEmpty())
                label += " (" + m.getCodigo() + ")";
            nombres.add(label);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerMaterias.setAdapter(adapter);

        spinnerMaterias.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                        materiaSeleccionada = listaMaterias.get(pos);
                        // Ocultar QR anterior al cambiar materia
                        cardQR.setVisibility(View.GONE);
                        tvInfoSesion.setVisibility(View.GONE);
                        tvTemporizador.setVisibility(View.GONE);
                        if (timer != null) timer.cancel();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> p) {}
                });

        materiaSeleccionada = listaMaterias.get(0);
    }

    private void generarQR() {
        if (materiaSeleccionada == null) return;

        // Cancelar timer anterior
        if (timer != null) timer.cancel();

        // Fecha y hora actual
        String fecha = new SimpleDateFormat("yyyy-MM-dd",
                new Locale("es", "ES")).format(new Date());
        String hora  = new SimpleDateFormat("HH:mm",
                new Locale("es", "ES")).format(new Date());

        // Contenido del QR: pipe-separated para fácil parseo
        // Formato: EDUTRACK|materia_id|materia_nombre|fecha|hora|timestamp
        String contenidoQR = "edutrack://asistencia/" +
                materiaSeleccionada.getId() +
                "/" + materiaSeleccionada.getNombre().replace(" ", "_") +
                "/" + fecha +
                "/" + hora +
                "/" + System.currentTimeMillis();        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(
                    contenidoQR, BarcodeFormat.QR_CODE, 600, 600);

            imgQR.setImageBitmap(bitmap);
            cardQR.setVisibility(View.VISIBLE);

            // Info de sesión
            tvInfoSesion.setText("📚 " + materiaSeleccionada.getNombre() +
                    "\n📅 " + fecha + "  🕐 " + hora);
            tvInfoSesion.setVisibility(View.VISIBLE);
            tvTemporizador.setVisibility(View.VISIBLE);

            // Registrar en BD
            dbHelper.registrarAsistencia(
                    materiaSeleccionada.getId(), fecha, hora, 0, 0);

            // Temporizador de 5 minutos
            iniciarTemporizador();

        } catch (Exception e) {
            Toast.makeText(this, "Error al generar QR: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarTemporizador() {
        timer = new CountDownTimer(QR_DURACION_MS, 1000) {
            @Override
            public void onTick(long ms) {
                long minutos  = ms / 60000;
                long segundos = (ms % 60000) / 1000;
                tvTemporizador.setText(
                        String.format(Locale.getDefault(),
                                "⏱ QR válido: %02d:%02d", minutos, segundos));
            }
            @Override
            public void onFinish() {
                tvTemporizador.setText("❌ QR expirado — genera uno nuevo");
                cardQR.setVisibility(View.GONE);
                tvTemporizador.setTextColor(0xFFFF5252);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
