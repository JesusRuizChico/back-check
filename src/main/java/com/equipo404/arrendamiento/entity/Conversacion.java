package com.equipo404.arrendamiento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Table(name = "conversaciones")
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Long idConversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_1", nullable = false)
    private Usuario usuario1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_2", nullable = false)
    private Usuario usuario2;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_ultimo_mensaje")
    private OffsetDateTime fechaUltimoMensaje;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "activa";

    protected Conversacion() {
    }

    public Conversacion(Usuario usuario1, Usuario usuario2) {
        this.usuario1 = usuario1;
        this.usuario2 = usuario2;
    }

    @PrePersist
    protected void prepararPersistencia() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
        if (estado == null) {
            estado = "activa";
        }
    }

    public Long getIdConversacion() { return idConversacion; }
    public Usuario getUsuario1() { return usuario1; }
    public Usuario getUsuario2() { return usuario2; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaUltimoMensaje() { return fechaUltimoMensaje; }
    public String getEstado() { return estado; }
}
