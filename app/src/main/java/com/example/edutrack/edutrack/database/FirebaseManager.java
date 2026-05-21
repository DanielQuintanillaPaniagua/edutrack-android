package com.example.edutrack.edutrack.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        String docId = String.valueOf(asistenciaId);
        db.collection("asistencia").document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("materia_id", materiaId);
                        data.put("fecha",      fecha);
                        data.put("hora",       hora);
                        data.put("total",      total);
                        data.put("presentes",  presentes);
                        db.collection("asistencia").document(docId).set(data);
                    }
                });
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
    //  FIRESTORE — Sincronizar inscripción (por correo)
    // ══════════════════════════════════════════════
    public static void sincronizarInscripcion(int estudianteId, int materiaId,
                                              String correoEstudiante) {
        String docId = correoEstudiante.replace(".", "_") + "_" + materiaId;
        Map<String, Object> data = new HashMap<>();
        data.put("correo_estudiante", correoEstudiante);
        data.put("materia_id",        materiaId);
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
    //  FIRESTORE — Descargar estudiantes
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

    // ══════════════════════════════════════════════
    //  FIRESTORE — Descargar materias
    // ══════════════════════════════════════════════
    public static void descargarMaterias(android.content.Context context,
                                         SyncCallback callback) {
        DatabaseHelper localDb = new DatabaseHelper(context);
        db.collection("materias")
                .get()
                .addOnSuccessListener(result -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : result) {
                        String nombre      = doc.getString("nombre");
                        String codigo      = doc.getString("codigo");
                        String descripcion = doc.getString("descripcion");
                        Long docenteIdLong = doc.getLong("docente_id");
                        int docenteId      = docenteIdLong != null ? docenteIdLong.intValue() : 0;
                        int materiaId      = Integer.parseInt(doc.getId());

                        android.database.Cursor c = localDb.getReadableDatabase()
                                .rawQuery("SELECT id FROM materias WHERE id = ?",
                                        new String[]{String.valueOf(materiaId)});
                        if (c.getCount() == 0) {
                            android.content.ContentValues v = new android.content.ContentValues();
                            v.put("id",          materiaId);
                            v.put("nombre",      nombre);
                            v.put("codigo",      codigo);
                            v.put("descripcion", descripcion);
                            v.put("docente_id",  docenteId);
                            localDb.getWritableDatabase().insert("materias", null, v);
                        }
                        c.close();
                    }
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // ══════════════════════════════════════════════
    //  FIRESTORE — Descargar inscripciones por correo
    // ══════════════════════════════════════════════
    public static void descargarInscripcionesEstudiante(android.content.Context context,
                                                        String correoEstudiante,
                                                        SyncCallback callback) {
        DatabaseHelper localDb = new DatabaseHelper(context);

        db.collection("inscripciones")
                .whereEqualTo("correo_estudiante", correoEstudiante)
                .get()
                .addOnSuccessListener(result -> {
                    // Obtener ID local del estudiante por correo
                    android.database.Cursor c = localDb.obtenerUsuario(correoEstudiante);
                    if (!c.moveToFirst()) { c.close(); return; }
                    int estudianteId = c.getInt(c.getColumnIndexOrThrow("id"));
                    c.close();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : result) {
                        Long materiaIdLong = doc.getLong("materia_id");
                        if (materiaIdLong == null) continue;
                        int materiaId = materiaIdLong.intValue();

                        if (!localDb.estaInscrito(estudianteId, materiaId)) {
                            localDb.inscribirEstudiante(estudianteId, materiaId);
                        }
                    }
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
    // ══════════════════════════════════════════════
//  FIRESTORE — Descargar sesiones de asistencia (docente)
// ══════════════════════════════════════════════
    public static void descargarAsistenciaDocente(android.content.Context context,
                                                  SyncCallback callback) {
        DatabaseHelper localDb = new DatabaseHelper(context);

        db.collection("asistencia")
                .get()
                .addOnSuccessListener(result -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : result) {
                        Long materiaIdLong = doc.getLong("materia_id");
                        String fecha       = doc.getString("fecha");
                        String hora        = doc.getString("hora");
                        Long totalLong     = doc.getLong("total");
                        Long presentesLong = doc.getLong("presentes");

                        if (materiaIdLong == null || fecha == null) continue;

                        int materiaId  = materiaIdLong.intValue();
                        int total      = totalLong != null ? totalLong.intValue() : 0;
                        int presentes  = presentesLong != null ? presentesLong.intValue() : 0;

                        android.database.Cursor c = localDb.getReadableDatabase()
                                .rawQuery("SELECT id FROM asistencia WHERE materia_id=? AND fecha=?",
                                        new String[]{String.valueOf(materiaId), fecha});
                        if (c.getCount() == 0) {
                            android.content.ContentValues v = new android.content.ContentValues();
                            v.put("materia_id", materiaId);
                            v.put("fecha",      fecha);
                            v.put("hora",       hora);
                            v.put("total",      total);
                            v.put("presentes",  presentes);
                            localDb.getWritableDatabase().insert("asistencia", null, v);
                        } else {
                            android.content.ContentValues v = new android.content.ContentValues();
                            v.put("total",     total);
                            v.put("presentes", presentes);
                            localDb.getWritableDatabase().update("asistencia", v,
                                    "materia_id=? AND fecha=?",
                                    new String[]{String.valueOf(materiaId), fecha});
                        }
                        c.close();
                    }
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}