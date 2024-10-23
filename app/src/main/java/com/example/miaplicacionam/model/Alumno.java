package com.example.miaplicacionam.model;

import java.io.Serializable;

public class Alumno implements Serializable {
  private String nombre;
  private String apellido;
  private Integer edad;
  private String imageURL;
  private String curso;
  private String id;

  public Alumno(String id, String nombre, String apellido, String curso, String imageURL, Integer edad) {
    setId(id);
    setImageURL(imageURL);
    setNombre(nombre);
    setApellido(apellido);
    setEdad(edad);
    setCurso(curso);
  }

  // Setters y Getters

  public String getImageURL() {
    return imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getEdad() {
    return edad;
  }

  public void setEdad(Integer edad) {
    this.edad = edad;
  }

  public String getNombre() {
    return nombre;
  }

  private void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public String getCurso() {
    return curso;
  }

  public void setCurso(String curso) {
    this.curso = curso;
  }

  public String getFullName() {
    return getApellido().toUpperCase() + ", " + getNombre();
  }

}