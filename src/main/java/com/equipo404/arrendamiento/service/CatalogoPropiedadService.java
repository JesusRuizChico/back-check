package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import java.util.List;

/** Consultas de propiedades publicadas que realiza cualquier usuario autenticado. */
public interface CatalogoPropiedadService {

    List<PropiedadResponse> obtenerCatalogoDisponible(Long idUsuario);

    PropiedadResponse obtenerDetalle(Long idPropiedad, Long idUsuario);
}
