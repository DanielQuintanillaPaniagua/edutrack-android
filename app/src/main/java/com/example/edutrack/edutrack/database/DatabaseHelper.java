package com.example.edutrack.edutrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "edutrack.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_SESION = "sesion";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SESION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "correo TEXT," +
                "rol TEXT," +
                "carnet TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESION);
        onCreate(db);
    }

    // Guardar sesión
    public void guardarSesion(String nombre, String correo, String rol, String carnet) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESION); // Solo una sesión activa
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("rol", rol);
        values.put("carnet", carnet);
        db.insert(TABLE_SESION, null, values);
        db.close();
    }

    // Obtener sesión activa
    public Cursor obtenerSesion() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SESION + " LIMIT 1", null);
    }

    // Cerrar sesión
    public void cerrarSesion() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESION);
        db.close();
    }

    // Verificar si hay sesión activa
    public boolean haySesionActiva() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SESION, null);
        boolean activa = cursor.getCount() > 0;
        cursor.close();
        return activa;
    }
}