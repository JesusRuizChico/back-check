package com.equipo404.arrendamiento.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propiedades")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_propiedad")
    private Long idPropiedad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_arrendador", nullable = false)
    private Usuario arrendador;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "ubicacion", nullable = false, length = 255)
    private String ubicacion;

    @Column(name = "habitaciones", nullable = false)
    private Integer habitaciones = 1;

    @Column(name = "servicios", columnDefinition = "TEXT")
    private String servicios;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "activa";

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private OffsetDateTime fechaPublicacion;

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropiedadImagen> imagenes = new ArrayList<>();

    public Propiedad() {}

    @PrePersist
    protected void prePersist() {
        if (fechaPublicacion == null) {
            fechaPublicacion = OffsetDateTime.now();
        }
    }

    public void addImagen(String url) {
        PropiedadImagen imagen = new PropiedadImagen(url, this);
        imagenes.add(imagen);
    }

    // Getters and setters
    public Long getIdPropiedad() { return idPropiedad; }
    public void setIdPropiedad(Long idPropiedad) { this.idPropiedad = idPropiedad; }
    public Usuario getArrendador() { return arrendador; }
    public void setArrendador(Usuario arrendador) { this.arrendador = arrendador; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public Integer getHabitaciones() { return habitaciones; }
    public void setHabitaciones(Integer habitaciones) { this.habitaciones = habitaciones; }
    public String getServicios() { return servicios; }
    public void setServicios(String servicios) { this.servicios = servicios; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public OffsetDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(OffsetDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public List<PropiedadImagen> getImagenes() { return imagenes; }
    public void setImagenes(List<PropiedadImagen> imagenes) { this.imagenes = imagenes; }
}
