package com.example.edutrack.edutrack.models;

public class Usuario {

    private int    id;
    private String nombre;
    private String correo;
    private String password;
    private String rol;
    private String carnet;

    public Usuario() {}

    public Usuario(String nombre, String correo, String password,
                   String rol, String carnet) {
        this.nombre   = nombre;
        this.correo   = correo;
        this.password = password;
        this.rol      = rol;
        this.carnet   = carnet;
    }

    public int    getId()       { return id; }
    public String getNombre()   { return nombre; }
    public String getCorreo()   { return correo; }
    public String getPassword() { return password; }
    public String getRol()      { return rol; }
    public String getCarnet()   { return carnet; }

    public void setId(int id)          { this.id = id; }
    public void setNombre(String v)    { this.nombre = v; }
    public void setCorreo(String v)    { this.correo = v; }
    public void setPassword(String v)  { this.password = v; }
    public void setRol(String v)       { this.rol = v; }
    public void setCarnet(String v)    { this.carnet = v; }
}

