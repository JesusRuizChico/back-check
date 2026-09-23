package com.equipo404.arrendamiento.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")

public class Rol{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre", nullable = false, unique = true, length = 400)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;


    public Rol(){

    }

    public Long getIdRol(){
        return idRol;
    }

    public void setIdRol(Long idRol){
        this.idRol = idRol;
    }

    public String getNombre(){
        return nombre;
    }

    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    public String getDescripcion(){
        return descripcion;
    }

    public void setDescripcion(String descripcion){
        this.descripcion = descripcion;
    }
}
