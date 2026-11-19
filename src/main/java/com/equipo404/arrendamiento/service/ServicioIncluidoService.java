package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.response.ServicioResponse;
import java.util.List;

/** Catálogo de servicios incluidos (amenidades) que puede tener una propiedad. */
public interface ServicioIncluidoService {

    List<ServicioResponse> listarActivos();
}
