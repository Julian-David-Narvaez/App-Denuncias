package com.example.bioterra;


public class Canjeo {
    private String id;
    private String nombre;
    private String direccion;
    private String idItem;
    private String ciudad;
    private String numeroC;

    public Canjeo() {
        // Constructor vacío requerido por Firebase
    }

    public Canjeo(String id, String nombre, String direccion, String idItem, String ciudad, String numeroC) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.idItem = idItem;
        this.ciudad = ciudad;
        this.numeroC = numeroC;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }
    public String getIdItem(){
        return idItem;
    }
    public String getCiudad(){
        return ciudad;
    }
    public String getNumeroC(){
        return numeroC;
    }
}