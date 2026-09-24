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
    @JoinColumn(name = "id_propietario", nullable = false)
    private Usuario propietario;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_mensual", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioMensual;

    @Column(name = "calle", nullable = false, length = 150)
    private String calle;

    @Column(name = "numero_exterior", nullable = false, length = 20)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 20)
    private String numeroInterior;

    @Column(name = "colonia", nullable = false, length = 100)
    private String colonia;

    @Column(name = "municipio", nullable = false, length = 100)
    private String municipio;

    @Column(name = "estado_ubicacion", nullable = false, length = 100)
    private String estadoUbicacion;

    @Column(name = "codigo_postal", nullable = false, length = 5)
    private String codigoPostal;

    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "disponible";

    @Column(name = "verificada", nullable = false)
    private Boolean verificada = false;

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private OffsetDateTime fechaPublicacion;

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropiedadFotografia> fotografias = new ArrayList<>();

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropiedadServicio> servicios = new ArrayList<>();

    public Propiedad() {}

    @PrePersist
    protected void prePersist() {
        if (fechaPublicacion == null) {
            fechaPublicacion = OffsetDateTime.now();
        }
    }

    public void addFotografia(String url, Short orden) {
        PropiedadFotografia fotografia = new PropiedadFotografia(url, this, orden);
        fotografias.add(fotografia);
    }

    // Getters and setters
    public Long getIdPropiedad() { return idPropiedad; }
    public void setIdPropiedad(Long idPropiedad) { this.idPropiedad = idPropiedad; }
    
    public Usuario getPropietario() { return propietario; }
    public void setPropietario(Usuario propietario) { this.propietario = propietario; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public BigDecimal getPrecioMensual() { return precioMensual; }
    public void setPrecioMensual(BigDecimal precioMensual) { this.precioMensual = precioMensual; }
    
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    
    public String getNumeroExterior() { return numeroExterior; }
    public void setNumeroExterior(String numeroExterior) { this.numeroExterior = numeroExterior; }
    
    public String getNumeroInterior() { return numeroInterior; }
    public void setNumeroInterior(String numeroInterior) { this.numeroInterior = numeroInterior; }
    
    public String getColonia() { return colonia; }
    public void setColonia(String colonia) { this.colonia = colonia; }
    
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    
    public String getEstadoUbicacion() { return estadoUbicacion; }
    public void setEstadoUbicacion(String estadoUbicacion) { this.estadoUbicacion = estadoUbicacion; }
    
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
    
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Boolean getVerificada() { return verificada; }
    public void setVerificada(Boolean verificada) { this.verificada = verificada; }
    
    public OffsetDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(OffsetDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    
    public List<PropiedadFotografia> getFotografias() { return fotografias; }
    public void setFotografias(List<PropiedadFotografia> fotografias) { this.fotografias = fotografias; }
    
    public List<PropiedadServicio> getServicios() { return servicios; }
    public void setServicios(List<PropiedadServicio> servicios) { this.servicios = servicios; }
}
