package com.arrendamiento.api.models;

import lombok.Data;
import java.util.List;
import java.util.Date;

@Data
public class Usuario {
    private String id;
    private String nombre;
    private String correo;
    private String telefono;
    private String correoAlterno;
    private String fotoPerfil;
    private String rol;
    private List<String> especialidades;
    private String disponibilidad;
    private Date ultimoAcceso;
}
