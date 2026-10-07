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
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long idMensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversacion", nullable = false)
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emisor", nullable = false)
    private Usuario emisor;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private OffsetDateTime fechaEnvio;

    @Column(name = "fecha_lectura")
    private OffsetDateTime fechaLectura;

    protected Mensaje() {
    }

    public Mensaje(Conversacion conversacion, Usuario emisor, String contenido) {
        this.conversacion = conversacion;
        this.emisor = emisor;
        this.contenido = contenido;
    }

    @PrePersist
    protected void prepararPersistencia() {
        if (fechaEnvio == null) {
            fechaEnvio = OffsetDateTime.now();
        }
    }

    public Long getIdMensaje() { return idMensaje; }
    public Conversacion getConversacion() { return conversacion; }
    public Usuario getEmisor() { return emisor; }
    public String getContenido() { return contenido; }
    public OffsetDateTime getFechaEnvio() { return fechaEnvio; }
    public OffsetDateTime getFechaLectura() { return fechaLectura; }
}
