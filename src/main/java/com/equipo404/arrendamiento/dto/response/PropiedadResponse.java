package com.equipo404.arrendamiento.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PropiedadResponse(
        Long idPropiedad,
        String titulo,
        String descripcion,
        BigDecimal precioMensual,
        String calle,
        String numeroExterior,
        String numeroInterior,
        String colonia,
        String municipio,
        String estadoUbicacion,
        String codigoPostal,
        BigDecimal latitud,
        BigDecimal longitud,
        Short habitaciones,
        Short banos,
        Short estacionamientos,
        BigDecimal metrosCuadrados,
        String estado,
        Boolean verificada,
        OffsetDateTime fechaPublicacion,
        List<String> imagenes,
        List<ServicioResponse> servicios,
        ArrendadorResponse arrendador,
        boolean esFavorito) {
}
