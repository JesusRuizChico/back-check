package com.equipo404.arrendamiento.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "propiedad_servicio")
@IdClass(PropiedadServicioId.class)
public class PropiedadServicio {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_propiedad", nullable = false)
    private Propiedad propiedad;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    public PropiedadServicio() {}

    public PropiedadServicio(Propiedad propiedad, Servicio servicio) {
        this.propiedad = propiedad;
        this.servicio = servicio;
    }

    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
}
