package com.equipo404.arrendamiento.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "propiedad_fotografia")
public class PropiedadFotografia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fotografia")
    private Long idFotografia;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "orden")
    private Short orden;

    @Column(name = "fecha_carga", nullable = false, updatable = false)
    private OffsetDateTime fechaCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_propiedad", nullable = false)
    private Propiedad propiedad;

    public PropiedadFotografia() {}

    public PropiedadFotografia(String url, Propiedad propiedad, Short orden) {
        this.url = url;
        this.propiedad = propiedad;
        this.orden = orden;
    }

    @PrePersist
    protected void prePersist() {
        if (fechaCarga == null) {
            fechaCarga = OffsetDateTime.now();
        }
    }

    public Long getIdFotografia() { return idFotografia; }
    public void setIdFotografia(Long idFotografia) { this.idFotografia = idFotografia; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public Short getOrden() { return orden; }
    public void setOrden(Short orden) { this.orden = orden; }
    
    public OffsetDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(OffsetDateTime fechaCarga) { this.fechaCarga = fechaCarga; }
    
    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }
}
