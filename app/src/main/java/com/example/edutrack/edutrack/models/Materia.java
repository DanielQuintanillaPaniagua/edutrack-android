package com.example.edutrack.edutrack.models;

public class Materia {

    private int    id;
    private String nombre;
    private String codigo;
    private String descripcion;
    private int    docenteId;

    public Materia() {}

    public Materia(String nombre, String codigo, String descripcion, int docenteId) {
        this.nombre      = nombre;
        this.codigo      = codigo;
        this.descripcion = descripcion;
        this.docenteId   = docenteId;
    }

    public int    getId()           { return id; }
    public String getNombre()       { return nombre; }
    public String getCodigo()       { return codigo; }
    public String getDescripcion()  { return descripcion; }
    public int    getDocenteId()    { return docenteId; }

    public void setId(int id)              { this.id = id; }
    public void setNombre(String v)        { this.nombre = v; }
    public void setCodigo(String v)        { this.codigo = v; }
    public void setDescripcion(String v)   { this.descripcion = v; }
    public void setDocenteId(int v)        { this.docenteId = v; }
}



