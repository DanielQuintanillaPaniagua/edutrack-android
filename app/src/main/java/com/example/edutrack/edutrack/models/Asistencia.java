package com.example.edutrack.edutrack.models;

public class Asistencia {
    private int    id;
    private int    materiaId;
    private String materiaNombre;
    private String fecha;
    private String hora;
    private int    total;
    private int    presentes;

    public Asistencia() {}

    public int    getId()            { return id; }
    public int    getMateriaId()     { return materiaId; }
    public String getMateriaNombre() { return materiaNombre; }
    public String getFecha()         { return fecha; }
    public String getHora()          { return hora; }
    public int    getTotal()         { return total; }
    public int    getPresentes()     { return presentes; }

    public void setId(int v)               { this.id = v; }
    public void setMateriaId(int v)        { this.materiaId = v; }
    public void setMateriaNombre(String v) { this.materiaNombre = v; }
    public void setFecha(String v)         { this.fecha = v; }
    public void setHora(String v)          { this.hora = v; }
    public void setTotal(int v)            { this.total = v; }
    public void setPresentes(int v)        { this.presentes = v; }

    public int getPorcentaje() {
        if (total == 0) return 0;
        return (presentes * 100) / total;
    }
}