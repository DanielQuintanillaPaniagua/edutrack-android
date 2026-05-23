package com.example.edutrack.edutrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.edutrack.edutrack.models.Asistencia;
import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "edutrack.db";
    private static final int DB_VERSION = 8;

    private static final String TABLE_SESION = "sesion";
    private static final String TABLE_USUARIOS = "usuarios";
    private static final String TABLE_MATERIAS = "materias";
    private static final String TABLE_ASISTENCIA = "asistencia";
    private static final String TABLE_INSCRIPCIONES = "inscripciones";
    private static final String TABLE_ASIST_ESTUDIANTE = "asistencia_estudiante";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ══════════════════════════════════════════
    //  CREACIÓN
    // ══════════════════════════════════════════

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SESION + " (" +
                "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER," +
                "nombre     TEXT," +
                "correo     TEXT," +
                "rol        TEXT," +
                "carnet     TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_USUARIOS + " (" +
                "id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre   TEXT," +
                "correo   TEXT UNIQUE," +
                "password TEXT," +
                "rol      TEXT," +
                "carnet   TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_MATERIAS + " (" +
                "id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre      TEXT NOT NULL," +
                "codigo      TEXT," +
                "descripcion TEXT," +
                "docente_id  INTEGER," +
                "FOREIGN KEY (docente_id) REFERENCES " + TABLE_USUARIOS + "(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_ASISTENCIA + " (" +
                "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "materia_id INTEGER," +
                "fecha      TEXT," +
                "hora       TEXT," +
                "total      INTEGER DEFAULT 0," +
                "presentes  INTEGER DEFAULT 0," +
                "FOREIGN KEY (materia_id) REFERENCES " + TABLE_MATERIAS + "(id))");

        // Estudiante inscrito a una materia
        db.execSQL("CREATE TABLE " + TABLE_INSCRIPCIONES + " (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "estudiante_id INTEGER NOT NULL," +
                "materia_id    INTEGER NOT NULL," +
                "UNIQUE(estudiante_id, materia_id)," +
                "FOREIGN KEY (estudiante_id) REFERENCES " + TABLE_USUARIOS + "(id) ON DELETE CASCADE," +
                "FOREIGN KEY (materia_id)    REFERENCES " + TABLE_MATERIAS + "(id) ON DELETE CASCADE)");

        // Registro individual de asistencia por estudiante
        db.execSQL("CREATE TABLE " + TABLE_ASIST_ESTUDIANTE + " (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "estudiante_id INTEGER NOT NULL," +
                "materia_id    INTEGER NOT NULL," +
                "fecha         TEXT    NOT NULL," +
                "hora          TEXT," +
                "estado        TEXT    DEFAULT 'presente'," +
                "FOREIGN KEY (estudiante_id) REFERENCES " + TABLE_USUARIOS + "(id)," +
                "FOREIGN KEY (materia_id)    REFERENCES " + TABLE_MATERIAS + "(id))");
    }

    // ══════════════════════════════════════════
    //  MIGRACIÓN
    // ══════════════════════════════════════════

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_SESION + " ADD COLUMN id_usuario INTEGER");
            } catch (Exception ignored) {
            }
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MATERIAS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL," +
                    "codigo TEXT, descripcion TEXT, docente_id INTEGER)");
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ASISTENCIA + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, materia_id INTEGER," +
                    "fecha TEXT, hora TEXT, total INTEGER DEFAULT 0, presentes INTEGER DEFAULT 0)");
        }
        if (oldVersion < 8) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_INSCRIPCIONES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "estudiante_id INTEGER NOT NULL," +
                    "materia_id    INTEGER NOT NULL," +
                    "UNIQUE(estudiante_id, materia_id))");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ASIST_ESTUDIANTE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "estudiante_id INTEGER NOT NULL," +
                    "materia_id    INTEGER NOT NULL," +
                    "fecha TEXT NOT NULL," +
                    "hora  TEXT," +
                    "estado TEXT DEFAULT 'presente')");
        }
    }

    // ══════════════════════════════════════════
    //  SESIÓN
    // ══════════════════════════════════════════

    public void guardarSesion(int idUsuario, String nombre, String correo,
                              String rol, String carnet) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESION);
        ContentValues v = new ContentValues();
        v.put("id_usuario", idUsuario);
        v.put("nombre", nombre);
        v.put("correo", correo);
        v.put("rol", rol);
        v.put("carnet", carnet);
        db.insert(TABLE_SESION, null, v);
        db.close();
    }

    public Usuario obtenerUsuarioSesion() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SESION + " LIMIT 1", null);
        if (cursor.moveToFirst()) {
            Usuario u = new Usuario();
            u.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario")));
            u.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            u.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
            u.setRol(cursor.getString(cursor.getColumnIndexOrThrow("rol")));
            u.setCarnet(cursor.getString(cursor.getColumnIndexOrThrow("carnet")));
            cursor.close();
            db.close();
            return u;
        }
        cursor.close();
        db.close();
        return null;
    }

    public Cursor obtenerSesion() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SESION + " LIMIT 1", null);
    }

    public void cerrarSesion() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESION);
        db.close();
    }

    public boolean haySesionActiva() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SESION, null);
        boolean activa = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return activa;
    }

    // ══════════════════════════════════════════
    //  USUARIOS
    // ══════════════════════════════════════════

    public void registrarUsuario(String nombre, String correo,
                                 String password, String rol, String carnet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        v.put("correo", correo);
        v.put("password", password);
        v.put("rol", rol);
        v.put("carnet", carnet);
        db.insert(TABLE_USUARIOS, null, v);
        db.close();
    }

    public boolean verificarCredenciales(String correo, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USUARIOS + " WHERE correo=? AND password=?",
                new String[]{correo, password});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    public Cursor obtenerUsuario(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE correo=?",
                new String[]{correo});
    }

    public void cambiarPassword(String correo, String nuevaPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("password", nuevaPassword);
        db.update(TABLE_USUARIOS, v, "correo = ?", new String[]{correo});
        db.close();
    }

    // ══════════════════════════════════════════
    //  MATERIAS (DOCENTE)
    // ══════════════════════════════════════════

    public long agregarMateria(Materia materia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("nombre", materia.getNombre());
        v.put("codigo", materia.getCodigo());
        v.put("descripcion", materia.getDescripcion());
        v.put("docente_id", materia.getDocenteId());
        long id = db.insert(TABLE_MATERIAS, null, v);
        db.close();
        return id;
    }

    public List<Materia> obtenerMateriasPorDocente(int docenteId) {
        List<Materia> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MATERIAS, null,
                "docente_id = ?", new String[]{String.valueOf(docenteId)},
                null, null, "nombre ASC");
        if (cursor.moveToFirst()) {
            do {
                Materia m = new Materia();
                m.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                m.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                m.setCodigo(cursor.getString(cursor.getColumnIndexOrThrow("codigo")));
                m.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow("descripcion")));
                m.setDocenteId(cursor.getInt(cursor.getColumnIndexOrThrow("docente_id")));
                lista.add(m);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    public boolean actualizarMateria(Materia materia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("nombre", materia.getNombre());
        v.put("codigo", materia.getCodigo());
        v.put("descripcion", materia.getDescripcion());
        int rows = db.update(TABLE_MATERIAS, v, "id = ?",
                new String[]{String.valueOf(materia.getId())});
        db.close();
        return rows > 0;
    }

    public boolean eliminarMateria(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_MATERIAS, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    // ══════════════════════════════════════════
    //  ASISTENCIA (DOCENTE - sesión)
    // ══════════════════════════════════════════

    public long registrarAsistencia(int materiaId, String fecha,
                                    String hora, int total, int presentes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("materia_id", materiaId);
        v.put("fecha", fecha);
        v.put("hora", hora);
        v.put("total", total);
        v.put("presentes", presentes);
        long id = db.insert(TABLE_ASISTENCIA, null, v);
        db.close();
        return id;
    }

    public List<Asistencia> obtenerHistorialPorDocente(int docenteId) {
        List<Asistencia> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT a.*, m.nombre as materia_nombre " +
                        "FROM " + TABLE_ASISTENCIA + " a " +
                        "INNER JOIN " + TABLE_MATERIAS + " m ON a.materia_id = m.id " +
                        "WHERE m.docente_id = ? " +
                        "ORDER BY a.fecha DESC, a.hora DESC",
                new String[]{String.valueOf(docenteId)});
        if (cursor.moveToFirst()) {
            do {
                Asistencia a = new Asistencia();
                a.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                a.setMateriaId(cursor.getInt(cursor.getColumnIndexOrThrow("materia_id")));
                a.setMateriaNombre(cursor.getString(cursor.getColumnIndexOrThrow("materia_nombre")));
                a.setFecha(cursor.getString(cursor.getColumnIndexOrThrow("fecha")));
                a.setHora(cursor.getString(cursor.getColumnIndexOrThrow("hora")));
                a.setTotal(cursor.getInt(cursor.getColumnIndexOrThrow("total")));
                a.setPresentes(cursor.getInt(cursor.getColumnIndexOrThrow("presentes")));
                lista.add(a);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // ══════════════════════════════════════════
    //  INSCRIPCIONES (ESTUDIANTE)
    // ══════════════════════════════════════════

    /**
     * Inscribe al estudiante en una materia. Ignora duplicados.
     */
    public boolean inscribirEstudiante(int estudianteId, int materiaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("estudiante_id", estudianteId);
        v.put("materia_id", materiaId);
        long result = db.insertWithOnConflict(TABLE_INSCRIPCIONES, null, v,
                SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result != -1;
    }

    public boolean estaInscrito(int estudianteId, int materiaId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_INSCRIPCIONES +
                        " WHERE estudiante_id=? AND materia_id=?",
                new String[]{String.valueOf(estudianteId), String.valueOf(materiaId)});
        boolean inscrito = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return inscrito;
    }

    /**
     * Retorna las materias en las que está inscrito el estudiante
     */
    public List<Materia> obtenerMateriasPorEstudiante(int estudianteId) {
        List<Materia> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT m.* FROM " + TABLE_MATERIAS + " m " +
                        "INNER JOIN " + TABLE_INSCRIPCIONES + " i ON m.id = i.materia_id " +
                        "WHERE i.estudiante_id = ? ORDER BY m.nombre ASC",
                new String[]{String.valueOf(estudianteId)});
        if (cursor.moveToFirst()) {
            do {
                Materia m = new Materia();
                m.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                m.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                m.setCodigo(cursor.getString(cursor.getColumnIndexOrThrow("codigo")));
                m.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow("descripcion")));
                m.setDocenteId(cursor.getInt(cursor.getColumnIndexOrThrow("docente_id")));
                lista.add(m);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // ══════════════════════════════════════════
    //  ASISTENCIA INDIVIDUAL (ESTUDIANTE)
    // ══════════════════════════════════════════

    /**
     * Registra la asistencia del estudiante al escanear el QR.
     * Evita doble registro en la misma sesión (misma fecha+materia).
     */
    public boolean registrarAsistenciaEstudiante(int estudianteId, int materiaId,
                                                 String fecha, String hora) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Verificar si ya registró asistencia hoy en esta materia
        Cursor dup = db.rawQuery(
                "SELECT id FROM " + TABLE_ASIST_ESTUDIANTE +
                        " WHERE estudiante_id=? AND materia_id=? AND fecha=?",
                new String[]{String.valueOf(estudianteId),
                        String.valueOf(materiaId), fecha});
        if (dup.getCount() > 0) {
            dup.close();
            db.close();
            return false;
        }
        dup.close();

        ContentValues v = new ContentValues();
        v.put("estudiante_id", estudianteId);
        v.put("materia_id", materiaId);
        v.put("fecha", fecha);
        v.put("hora", hora);
        v.put("estado", "presente");
        long id = db.insert(TABLE_ASIST_ESTUDIANTE, null, v);
        db.close();
        return id != -1;
    }

    /**
     * Historial de asistencias del estudiante (todas sus materias)
     */
    public Cursor obtenerHistorialEstudiante(int estudianteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT ae.*, m.nombre as materia_nombre " +
                        "FROM " + TABLE_ASIST_ESTUDIANTE + " ae " +
                        "INNER JOIN " + TABLE_MATERIAS + " m ON ae.materia_id = m.id " +
                        "WHERE ae.estudiante_id = ? " +
                        "ORDER BY ae.fecha DESC, ae.hora DESC",
                new String[]{String.valueOf(estudianteId)});
    }

    /**
     * Porcentaje de asistencia del estudiante en una materia.
     * Retorna valor entre 0 y 100. -1 si no hay datos.
     */
    public int obtenerPorcentajeAsistencia(int estudianteId, int materiaId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Total de sesiones registradas por el docente en esa materia
        Cursor total = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ASISTENCIA + " WHERE materia_id=?",
                new String[]{String.valueOf(materiaId)});
        int totalClases = 0;
        if (total.moveToFirst()) totalClases = total.getInt(0);
        total.close();

        if (totalClases == 0) {
            db.close();
            return -1;
        }

        // Clases a las que asistió el estudiante
        Cursor asistidas = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ASIST_ESTUDIANTE +
                        " WHERE estudiante_id=? AND materia_id=? AND estado='presente'",
                new String[]{String.valueOf(estudianteId), String.valueOf(materiaId)});
        int presentes = 0;
        if (asistidas.moveToFirst()) presentes = asistidas.getInt(0);
        asistidas.close();
        db.close();

        return (int) ((presentes * 100.0f) / totalClases);
    }

    /**
     * Lista de materias con % de asistencia del estudiante (para el Inicio y Mis Materias)
     */
    public List<Object[]> obtenerResumenAsistenciaEstudiante(int estudianteId) {
        // Retorna lista de {Materia, porcentaje(int), clasesAsistidas(int), totalClases(int)}
        List<Object[]> lista = new ArrayList<>();
        List<Materia> materias = obtenerMateriasPorEstudiante(estudianteId);
        for (Materia m : materias) {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cTotal = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_ASISTENCIA + " WHERE materia_id=?",
                    new String[]{String.valueOf(m.getId())});
            int total = 0;
            if (cTotal.moveToFirst()) total = cTotal.getInt(0);
            cTotal.close();

            Cursor cPres = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_ASIST_ESTUDIANTE +
                            " WHERE estudiante_id=? AND materia_id=? AND estado='presente'",
                    new String[]{String.valueOf(estudianteId), String.valueOf(m.getId())});
            int presentes = 0;
            if (cPres.moveToFirst()) presentes = cPres.getInt(0);
            cPres.close();
            db.close();

            int porcentaje = (total > 0) ? (int) ((presentes * 100.0f) / total) : 0;
            lista.add(new Object[]{m, porcentaje, presentes, total});
        }
        return lista;
    }

    // Obtener todos los usuarios con rol estudiante
    public List<Usuario> obtenerTodosLosEstudiantes() {
        List<Usuario> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USUARIOS + " WHERE rol = 'estudiante' ORDER BY nombre ASC",
                null);
        if (cursor.moveToFirst()) {
            do {
                Usuario u = new Usuario();
                u.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                u.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                u.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
                u.setCarnet(cursor.getString(cursor.getColumnIndexOrThrow("carnet")));
                lista.add(u);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    public boolean desinscribirEstudiante(int estudianteId, int materiaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_INSCRIPCIONES,
                "estudiante_id = ? AND materia_id = ?",
                new String[]{String.valueOf(estudianteId), String.valueOf(materiaId)});
        db.close();
        return rows > 0;
    }
    public long registrarObtenerAsistencia(int materiaId, String fecha, String hora) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id FROM asistencia WHERE materia_id=? AND fecha=?",
                new String[]{String.valueOf(materiaId), fecha});

        if (c.moveToFirst()) {
            long id = c.getLong(0);
            c.close();
            db.close();
            return id;
        }
        c.close();

        ContentValues v = new ContentValues();
        v.put("materia_id", materiaId);
        v.put("fecha",      fecha);
        v.put("hora",       hora);
        v.put("total",      0);
        v.put("presentes",  0);
        long id = db.insert("asistencia", null, v);
        db.close();
        return id;
    }
    public void actualizarConteoAsistencia(int materiaId, String fecha) {
        SQLiteDatabase db = getWritableDatabase();

        // Contar total de inscritos en la materia
        Cursor total = db.rawQuery(
                "SELECT COUNT(*) FROM inscripciones WHERE materia_id = ?",
                new String[]{String.valueOf(materiaId)});
        int totalEst = 0;
        if (total.moveToFirst()) totalEst = total.getInt(0);
        total.close();

        // Contar presentes hoy
        Cursor pres = db.rawQuery(
                "SELECT COUNT(*) FROM asistencia_estudiante " +
                        "WHERE materia_id=? AND fecha=? AND estado='presente'",
                new String[]{String.valueOf(materiaId), fecha});
        int presentes = 0;
        if (pres.moveToFirst()) presentes = pres.getInt(0);
        pres.close();

        // Actualizar tabla asistencia
        ContentValues v = new ContentValues();
        v.put("total",    totalEst);
        v.put("presentes", presentes);
        db.update("asistencia", v,
                "materia_id=? AND fecha=?",
                new String[]{String.valueOf(materiaId), fecha});
        db.close();
    }
    public int contarInscritosPorMateria(int materiaId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM inscripciones WHERE materia_id = ?",
                new String[]{String.valueOf(materiaId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        db.close();
        return count;
    }
    public void actualizarConteoSesion(int materiaId, String fecha,
                                       int presentes, int total) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("presentes", presentes);
        v.put("total", total);
        db.update("asistencia", v,
                "materia_id = ? AND fecha = ?",
                new String[]{String.valueOf(materiaId), fecha});
        db.close();
    }

    public Cursor obtenerAsistenciaConInasistencias(int estudianteId, String fecha) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT m.nombre as materia_nombre, " +
                        "       COALESCE(ae.hora, '--') as hora, " +
                        "       CASE WHEN ae.id IS NULL THEN 'ausente' ELSE 'presente' END as estado " +
                        "FROM asistencia a " +
                        "INNER JOIN materias m ON a.materia_id = m.id " +
                        "INNER JOIN inscripciones i ON i.materia_id = m.id AND i.estudiante_id = ? " +
                        "LEFT JOIN asistencia_estudiante ae " +
                        "       ON ae.materia_id = a.materia_id " +
                        "      AND ae.estudiante_id = ? " +
                        "      AND ae.fecha = a.fecha " +
                        "WHERE a.fecha = ? " +
                        "ORDER BY m.nombre ASC",
                new String[]{
                        String.valueOf(estudianteId),
                        String.valueOf(estudianteId),
                        fecha
                });
    }
    }
