package com.example.edutrack.edutrack.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ── Interfaces callback ──────────────────────────
    public interface AuthCallback {
        void onSuccess(String uid);
        void onError(String error);
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String error);
    }

    // ══════════════════════════════════════════════
    //  AUTH — Registro
    // ══════════════════════════════════════════════
    public static void registrarUsuario(String nombre, String correo,
                                        String password, String rol,
                                        String carnet, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(correo, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    // Guardar perfil en Firestore
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("nombre", nombre);
                    usuario.put("correo", correo);
                    usuario.put("rol",    rol);
                    usuario.put("carnet", carnet);

                    db.collection("usuarios").document(uid)
                            .set(usuario)
                            .addOnSuccessListener(v -> callback.onSuccess(uid))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ══════════════════════════════════════════════
    //  AUTH — Login
    // ══════════════════════════════════════════════
    public static void loginUsuario(String correo, String password,
                                    AuthCallback callback) {
        auth.signInWithEmailAndPassword(correo, password)
                .addOnSuccessListener(result ->
                        callback.onSuccess(result.getUser().getUid()))
                .addOnFailureListener(e ->
                        callback.onError(e.getMessage()));
    }

    // ══════════════════════════════════════════════
    //  FIRESTORE — Sincronizar materia
    // ══════════════════════════════════════════════
    public static void sincronizarMateria(int materiaId, String nombre,
                                          String codigo, String descripcion,
                                          int docenteId, SyncCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre",      nombre);
        data.put("codigo",      codigo);
        data.put("descripcion", descripcion);
        data.put("docente_id",  docenteId);

        db.collection("materias").document(String.valueOf(materiaId))
                .set(data)
                .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    // ══════════════════════════════════════════════
    //  FIRESTORE — Sincronizar asistencia (docente)
    // ══════════════════════════════════════════════
    public static void sincronizarAsistencia(long asistenciaId, int materiaId,
                                             String fecha, String hora,
                                             int total, int presentes) {
        Map<String, Object> data = new HashMap<>();
        data.put("materia_id", materiaId);
        data.put("fecha",      fecha);
        data.put("hora",       hora);
        data.put("total",      total);
        data.put("presentes",  presentes);

        db.collection("asistencia").document(String.valueOf(asistenciaId))
                .set(data);
    }

    // ══════════════════════════════════════════════
    //  FIRESTORE — Sincronizar asistencia estudiante
    // ══════════════════════════════════════════════
    public static void sincronizarAsistenciaEstudiante(int estudianteId,
                                                       int materiaId,
                                                       String fecha,
                                                       String hora,
                                                       String estado) {
        String docId = estudianteId + "_" + materiaId + "_" + fecha;

        Map<String, Object> data = new HashMap<>();
        data.put("estudiante_id", estudianteId);
        data.put("materia_id",    materiaId);
        data.put("fecha",         fecha);
        data.put("hora",          hora);
        data.put("estado",        estado);

        db.collection("asistencia_estudiante").document(docId).set(data);
    }

    // ══════════════════════════════════════════════
    //  FIRESTORE — Sincronizar inscripción
    // ══════════════════════════════════════════════
    public static void sincronizarInscripcion(int estudianteId, int materiaId) {
        String docId = estudianteId + "_" + materiaId;

        Map<String, Object> data = new HashMap<>();
        data.put("estudiante_id", estudianteId);
        data.put("materia_id",    materiaId);

        db.collection("inscripciones").document(docId).set(data);
    }

    // ══════════════════════════════════════════════
    //  AUTH — Cerrar sesión
    // ══════════════════════════════════════════════
    public static void cerrarSesion() {
        auth.signOut();
    }

    public static boolean hayUsuarioActivo() {
        return auth.getCurrentUser() != null;
    }
    // ══════════════════════════════════════════════
//  FIRESTORE — Descargar estudiantes al SQLite local
// ══════════════════════════════════════════════
    public static void descargarEstudiantes(android.content.Context context,
                                            SyncCallback callback) {
        DatabaseHelper localDb = new DatabaseHelper(context);

        db.collection("usuarios")
                .whereEqualTo("rol", "estudiante")
                .get()
                .addOnSuccessListener(result -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : result) {
                        String nombre = doc.getString("nombre");
                        String correo = doc.getString("correo");
                        String carnet = doc.getString("carnet");

                        // Solo insertar si no existe ya en SQLite
                        if (localDb.obtenerUsuario(correo).getCount() == 0) {
                            localDb.registrarUsuario(nombre, correo,
                                    "firebase_sync", "estudiante", carnet);
                        }
                    }
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}
