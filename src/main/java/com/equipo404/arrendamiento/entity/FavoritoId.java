package com.equipo404.arrendamiento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FavoritoId implements Serializable {

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_propiedad")
    private Long idPropiedad;

    protected FavoritoId() {
    }

    public FavoritoId(Long idUsuario, Long idPropiedad) {
        this.idUsuario = idUsuario;
        this.idPropiedad = idPropiedad;
    }

    public Long getIdUsuario() { return idUsuario; }
    public Long getIdPropiedad() { return idPropiedad; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoritoId that)) return false;
        return Objects.equals(idUsuario, that.idUsuario)
                && Objects.equals(idPropiedad, that.idPropiedad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idPropiedad);
    }
}
