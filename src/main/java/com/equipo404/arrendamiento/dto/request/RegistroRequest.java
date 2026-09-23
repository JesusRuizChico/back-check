package com.equipo404.arrendamiento.dto.request;

import jakarta.validation.constraints.*;

public class RegistroRequest {

    @NotBlank
    @Size(max = 150)
    private String nombre;
    @NotBlank
    @Email
    @Size(max = 254)
    private String correo;
    @NotBlank
    @Size(min = 6, max = 72)
    private String contrasena;
    @Size(max = 25)
    private String telefono;
    @NotBlank
    @Size(max = 400)
    private String rolSolicitado;

    public RegistroRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRolSolicitado() {
        return rolSolicitado;
    }

    public void setRolSolicitado(String rolSolicitado) {
        this.rolSolicitado = rolSolicitado;
    }
}