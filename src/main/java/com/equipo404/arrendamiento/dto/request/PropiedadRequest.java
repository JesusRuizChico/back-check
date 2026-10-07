package com.equipo404.arrendamiento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public class PropiedadRequest {
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
    
    @NotNull(message = "El precio mensual es obligatorio")
    @Positive
    @Digits(integer = 10, fraction = 2)
    private BigDecimal precioMensual;
    
    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 150)
    private String calle;
    
    @NotBlank(message = "El número exterior es obligatorio")
    @Size(max = 20)
    private String numeroExterior;
    
    @Size(max = 20)
    private String numeroInterior;
    
    @NotBlank(message = "La colonia es obligatoria")
    @Size(max = 100)
    private String colonia;
    
    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100)
    private String municipio;
    
    @NotBlank(message = "El estado (ubicación) es obligatorio")
    @Size(max = 100)
    private String estadoUbicacion;
    
    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^[0-9]{5}$")
    private String codigoPostal;
    
    @DecimalMin("-90")
    @DecimalMax("90")
    @Digits(integer = 3, fraction = 6)
    private BigDecimal latitud;

    @DecimalMin("-180")
    @DecimalMax("180")
    @Digits(integer = 3, fraction = 6)
    private BigDecimal longitud;
    
    private List<@NotNull @Positive Long> serviciosIds;

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
    public List<Long> getServiciosIds() { return serviciosIds; }
    public void setServiciosIds(List<Long> serviciosIds) { this.serviciosIds = serviciosIds; }
}
