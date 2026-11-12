package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.PropiedadRequest;
import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** Operaciones que el arrendador realiza sobre sus propias publicaciones. */
public interface GestionPropiedadService {

    PropiedadResponse publicarPropiedad(Long idArrendador, PropiedadRequest request, List<MultipartFile> imagenes);

    List<PropiedadResponse> obtenerMisPropiedades(Long idArrendador);

    PropiedadResponse cambiarEstado(Long idArrendador, Long idPropiedad, String nuevoEstado);

    void eliminarPropiedad(Long idArrendador, Long idPropiedad);
}
