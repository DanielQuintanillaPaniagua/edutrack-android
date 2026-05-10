package com.example.edutrack.edutrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.edutrack.edutrack.models.Materia;
import com.example.edutrack.edutrack.models.Usuario;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "edutrack.db";
    private static final int DB_VERSION = 4;

    private static final String TABLE_SESION = "sesion";
    private static final String TABLE_USUARIOS = "usuarios";
    private static final String TABLE_MATERIAS = "materias";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ══════════════════════════════════════════
    //  CREACIÓN
    // ══════════════════════════════════════════

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SESION + " (" +
                "id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario  INTEGER," +
                "nombre      TEXT," +
                "correo      TEXT," +
                "rol         TEXT," +
                "carnet      TEXT)");

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
    }

    // ══════════════════════════════════════════
    //  MIGRACIÓN
    // ══════════════════════════════════════════

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            // Agregar id_usuario a sesion sin borrar datos
            try {
                db.execSQL("ALTER TABLE " + TABLE_SESION + " ADD COLUMN id_usuario INTEGER");
            } catch (Exception ignored) {
            } // ya existe si se corre dos veces

            // Crear tabla materias si no existe
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MATERIAS + " (" +
                    "id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre      TEXT NOT NULL," +
                    "codigo      TEXT," +
                    "descripcion TEXT," +
                    "docente_id  INTEGER," +
                    "FOREIGN KEY (docente_id) REFERENCES " + TABLE_USUARIOS + "(id) ON DELETE CASCADE)");
        }
    }

    // ══════════════════════════════════════════
    //  SESIÓN
    // ══════════════════════════════════════════

    public void guardarSesion(int idUsuario, String nombre, String correo,
                              String rol, String carnet) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESION);
        ContentValues values = new ContentValues();
        values.put("id_usuario", idUsuario);
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("rol", rol);
        values.put("carnet", carnet);
        db.insert(TABLE_SESION, null, values);
        db.close();
    }

    public Usuario obtenerUsuarioSesion() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_SESION + " LIMIT 1", null);
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

    /**
     * Mantener compatibilidad con código que usa el Cursor directamente
     */
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


    public void registrarUsuario(String nombre, String correo,
                                 String password, String rol, String carnet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("password", password);
        values.put("rol", rol);
        values.put("carnet", carnet);
        db.insert(TABLE_USUARIOS, null, values);
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
        return db.rawQuery(
                "SELECT * FROM " + TABLE_USUARIOS + " WHERE correo=?",
                new String[]{correo});
    }


    public long agregarMateria(Materia materia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", materia.getNombre());
        values.put("codigo", materia.getCodigo());
        values.put("descripcion", materia.getDescripcion());
        values.put("docente_id", materia.getDocenteId());
        long id = db.insert(TABLE_MATERIAS, null, values);
        db.close();
        return id;
    }

    public List<Materia> obtenerMateriasPorDocente(int docenteId) {
        List<Materia> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_MATERIAS, null,
                "docente_id = ?",
                new String[]{String.valueOf(docenteId)},
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
        ContentValues values = new ContentValues();
        values.put("nombre", materia.getNombre());
        values.put("codigo", materia.getCodigo());
        values.put("descripcion", materia.getDescripcion());
        int rows = db.update(TABLE_MATERIAS, values,
                "id = ?", new String[]{String.valueOf(materia.getId())});
        db.close();
        return rows > 0;
    }

    public boolean eliminarMateria(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_MATERIAS,
                "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public void cambiarPassword(String correo, String nuevaPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", nuevaPassword);
        db.update(TABLE_USUARIOS, values, "correo = ?", new String[]{correo});
        db.close();
    }
}