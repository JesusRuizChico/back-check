package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import java.util.List;

public interface FavoritoService {

    List<PropiedadResponse> listar(Long idUsuario);

    void agregar(Long idUsuario, Long idPropiedad);

    void quitar(Long idUsuario, Long idPropiedad);
}
