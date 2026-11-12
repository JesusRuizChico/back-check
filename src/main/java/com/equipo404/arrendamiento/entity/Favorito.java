package com.equipo404.arrendamiento.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "favoritos")
public class Favorito {

    @EmbeddedId
    private FavoritoId id;

    @MapsId("idUsuario")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @MapsId("idPropiedad")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_propiedad", nullable = false)
    private Propiedad propiedad;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    private OffsetDateTime fechaAgregado;

    protected Favorito() {
    }

    public FavoritoId getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Propiedad getPropiedad() { return propiedad; }
    public OffsetDateTime getFechaAgregado() { return fechaAgregado; }
}
