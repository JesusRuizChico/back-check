package com.equipo404.arrendamiento.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "propiedad_imagenes")
public class PropiedadImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long idImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_propiedad", nullable = false)
    private Propiedad propiedad;

    @Column(name = "url_imagen", nullable = false, length = 500)
    private String urlImagen;

    public PropiedadImagen() {}

    public PropiedadImagen(String urlImagen, Propiedad propiedad) {
        this.urlImagen = urlImagen;
        this.propiedad = propiedad;
    }

    public Long getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Long idImagen) {
        this.idImagen = idImagen;
    }

    public Propiedad getPropiedad() {
        return propiedad;
    }

    public void setPropiedad(Propiedad propiedad) {
        this.propiedad = propiedad;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }
}
