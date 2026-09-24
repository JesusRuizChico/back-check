package com.equipo404.arrendamiento.entity;

import java.io.Serializable;
import java.util.Objects;

public class PropiedadServicioId implements Serializable {
    private Long propiedad;
    private Long servicio;

    public PropiedadServicioId() {}

    public PropiedadServicioId(Long propiedad, Long servicio) {
        this.propiedad = propiedad;
        this.servicio = servicio;
    }

    public Long getPropiedad() { return propiedad; }
    public void setPropiedad(Long propiedad) { this.propiedad = propiedad; }

    public Long getServicio() { return servicio; }
    public void setServicio(Long servicio) { this.servicio = servicio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropiedadServicioId that = (PropiedadServicioId) o;
        return Objects.equals(propiedad, that.propiedad) &&
               Objects.equals(servicio, that.servicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propiedad, servicio);
    }
}
