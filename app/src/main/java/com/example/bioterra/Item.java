package com.example.bioterra;


public class Item {
    private String id; // Cambiaremos el tipo de dato a String para usar con Firebase

    private String nombre;
    private double precio;
    private String imageUrl; // URL de la imagen, no un Bitmap directo
    // Constructor sin argumentos requerido por Firebase
    public Item() {
        // Constructor vacío requerido por Firebase para deserialización
    }

    // Constructor que acepta nombre, precio e imageUrl
    public Item(String nombre , double precio, String imageUrl ) {

        this.nombre = nombre;
        this.precio=precio;
        this.imageUrl=imageUrl;
    }


    // Getters y setters

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }


    public String getNombre() {

        return nombre;
    }

    public void setNombre(String nombre) {

        this.nombre = nombre;
    }
    public double getPrecio() {

        return precio;
    }

    public void setPrecio(double precio) {

        this.precio = precio;
    }
    public String getImageUrl() {

        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

}