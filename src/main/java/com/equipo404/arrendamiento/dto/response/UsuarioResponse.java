package com.equipo404.arrendamiento.dto.response;

import java.util.List;

public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String correo;
    private String telefono;
    private String fotoPerfil;
    private List<String> rolesActivos;

    public UsuarioResponse() {
    }

    public UsuarioResponse(
            Long id,
            String nombre,
            String correo,
            String telefono,
            String fotoPerfil,
            List<String> rolesActivos) {

        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.fotoPerfil = fotoPerfil;
        this.rolesActivos = rolesActivos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public List<String> getRolesActivos() {
        return rolesActivos;
    }

    public void setRolesActivos(List<String> rolesActivos) {
        this.rolesActivos = rolesActivos;
    }
}