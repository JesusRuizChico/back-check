package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.entity.Propiedad;
import com.equipo404.arrendamiento.exception.RecursoNoEncontradoException;
import com.equipo404.arrendamiento.mapper.PropiedadMapper;
import com.equipo404.arrendamiento.repository.FavoritoRepository;
import com.equipo404.arrendamiento.repository.PropiedadRepository;
import com.equipo404.arrendamiento.service.CatalogoPropiedadService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoPropiedadServiceImpl implements CatalogoPropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final FavoritoRepository favoritoRepository;

    public CatalogoPropiedadServiceImpl(
            PropiedadRepository propiedadRepository,
            FavoritoRepository favoritoRepository) {
        this.propiedadRepository = propiedadRepository;
        this.favoritoRepository = favoritoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropiedadResponse> obtenerCatalogoDisponible(Long idUsuario) {
        Set<Long> favoritos = favoritoRepository.idsPropiedadesFavoritas(idUsuario);
        return propiedadRepository.findByEstadoOrderByFechaPublicacionDesc(Propiedad.ESTADO_DISPONIBLE)
                .stream()
                .map(propiedad -> PropiedadMapper.toResponse(
                        propiedad, favoritos.contains(propiedad.getIdPropiedad())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PropiedadResponse obtenerDetalle(Long idPropiedad, Long idUsuario) {
        Propiedad propiedad = propiedadRepository.findById(idPropiedad)
                .filter(encontrada -> !encontrada.estaEliminada())
                .filter(encontrada -> Propiedad.ESTADO_DISPONIBLE.equals(encontrada.getEstado())
                        || encontrada.perteneceA(idUsuario))
                .orElseThrow(() -> new RecursoNoEncontradoException("La propiedad no existe."));
        boolean esFavorito = favoritoRepository.idsPropiedadesFavoritas(idUsuario)
                .contains(propiedad.getIdPropiedad());
        return PropiedadMapper.toResponse(propiedad, esFavorito);
    }
}
