package com.equipo404.arrendamiento.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class PropiedadResponse {
    private Long idPropiedad;
    private String titulo;
    private String descripcion;
    private BigDecimal precioMensual;
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String colonia;
    private String municipio;
    private String estadoUbicacion;
    private String codigoPostal;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String estado;
    private Boolean verificada;
    private OffsetDateTime fechaPublicacion;
    private List<String> imagenes;
    private List<String> servicios;

    public Long getIdPropiedad() { return idPropiedad; }
    public void setIdPropiedad(Long idPropiedad) { this.idPropiedad = idPropiedad; }
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
    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }
    public List<String> getServicios() { return servicios; }
    public void setServicios(List<String> servicios) { this.servicios = servicios; }
}
