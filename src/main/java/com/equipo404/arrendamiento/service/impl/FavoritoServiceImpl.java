package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.entity.FavoritoId;
import com.equipo404.arrendamiento.entity.Propiedad;
import com.equipo404.arrendamiento.exception.RecursoNoEncontradoException;
import com.equipo404.arrendamiento.mapper.PropiedadMapper;
import com.equipo404.arrendamiento.repository.FavoritoRepository;
import com.equipo404.arrendamiento.repository.PropiedadRepository;
import com.equipo404.arrendamiento.service.FavoritoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final PropiedadRepository propiedadRepository;

    public FavoritoServiceImpl(FavoritoRepository favoritoRepository, PropiedadRepository propiedadRepository) {
        this.favoritoRepository = favoritoRepository;
        this.propiedadRepository = propiedadRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropiedadResponse> listar(Long idUsuario) {
        return favoritoRepository
                .findByUsuario_IdUsuarioAndPropiedad_EstadoNotOrderByFechaAgregadoDesc(
                        idUsuario, Propiedad.ESTADO_ELIMINADA)
                .stream()
                .map(favorito -> PropiedadMapper.toResponse(favorito.getPropiedad(), true))
                .toList();
    }

    @Override
    @Transactional
    public void agregar(Long idUsuario, Long idPropiedad) {
        boolean disponible = propiedadRepository.findById(idPropiedad)
                .map(propiedad -> Propiedad.ESTADO_DISPONIBLE.equals(propiedad.getEstado()))
                .orElse(false);
        if (!disponible) {
            throw new RecursoNoEncontradoException("La propiedad no está disponible.");
        }
        favoritoRepository.agregarSiNoExiste(idUsuario, idPropiedad);
    }

    @Override
    @Transactional
    public void quitar(Long idUsuario, Long idPropiedad) {
        FavoritoId id = new FavoritoId(idUsuario, idPropiedad);
        if (favoritoRepository.existsById(id)) {
            favoritoRepository.deleteById(id);
        }
    }
}
