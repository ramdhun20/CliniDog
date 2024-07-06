package com.clinidog.clinidog.dto;
import java.util.Set;

public class MascotaRequest {

    private String nombre;
    private String raza;
    private int edad;
    private Set<String> vacunas;

    // Constructor, getters y setters
    public MascotaRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public Set<String> getVacunas() {
        return vacunas;
    }

    public void setVacunas(Set<String> vacunas) {
        this.vacunas = vacunas;
    }
}
