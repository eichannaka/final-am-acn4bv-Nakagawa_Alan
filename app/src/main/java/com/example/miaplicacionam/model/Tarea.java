package com.example.miaplicacionam.model;

import java.io.Serializable;
import com.google.firebase.Timestamp;

public class Tarea implements Serializable {
    private String id;
    private String titulo;
    private String descripcion;
    private Timestamp fechaCreacion;
    private Timestamp fechaVencimiento;
    private String estado;

    public Tarea(String id, String titulo, String descripcion, Timestamp fechaCreacion, Timestamp fechaVencimiento, String estado) {
        setId(id);
        setTitulo(titulo);
        setDescripcion(descripcion);
        setFechaCreacion(fechaCreacion);
        setFechaVencimiento(fechaVencimiento);
        setEstado(estado);
    }

    // Setters y Getters

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Timestamp getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Timestamp fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Método para obtener una representación en formato String del estado y fechas de la tarea
    public String getEstadoYFechas() {
        return "Estado: " + getEstado() + ", Creación: " + getFechaCreacion().toDate().toString() + ", Vencimiento: " + getFechaVencimiento().toDate().toString();
    }
}
