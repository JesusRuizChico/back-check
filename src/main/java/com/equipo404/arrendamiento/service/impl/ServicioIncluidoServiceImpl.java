package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.dto.response.ServicioResponse;
import com.equipo404.arrendamiento.mapper.PropiedadMapper;
import com.equipo404.arrendamiento.repository.ServicioRepository;
import com.equipo404.arrendamiento.service.ServicioIncluidoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioIncluidoServiceImpl implements ServicioIncluidoService {

    private final ServicioRepository servicioRepository;

    public ServicioIncluidoServiceImpl(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponse> listarActivos() {
        return servicioRepository.findByEstadoOrderByIdServicioAsc("activo").stream()
                .map(PropiedadMapper::toResponse)
                .toList();
    }
}
