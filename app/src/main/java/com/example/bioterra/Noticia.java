package com.example.bioterra;

public class Noticia {
    private String id;
    private String titulo;
    private String contenido;
    private String imageUrl;

    public Noticia() {
        // Constructor vacío requerido por Firebase para la deserialización
    }

    public Noticia(String id, String titulo, String contenido, String imageUrl) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}


