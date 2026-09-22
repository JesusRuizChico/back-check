package com.equipo404.arrendamiento.dto.request;

import jakarta.validation.constraints.*;

public class LoginRequest {

    @NotBlank
    @Email
    @Size(max = 254)
    private String correo;
    @NotBlank
    @Size(max = 72)
    private String contrasena;

    public LoginRequest() {
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
}