package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productos")
public class Product {
    @Id
    private String id;
    private String nombre;
    private Double precio;

    public String getNombre() {
        return nombre;
    }

    public String getId() {
        return id;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }
}
