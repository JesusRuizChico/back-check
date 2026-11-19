package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.dto.request.PropiedadRequest;
import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.entity.Propiedad;
import com.equipo404.arrendamiento.entity.Servicio;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.exception.RecursoNoEncontradoException;
import com.equipo404.arrendamiento.mapper.PropiedadMapper;
import com.equipo404.arrendamiento.repository.PropiedadRepository;
import com.equipo404.arrendamiento.repository.ServicioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.service.FileStorageService;
import com.equipo404.arrendamiento.service.GestionPropiedadService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GestionPropiedadServiceImpl implements GestionPropiedadService {

    private static final int MAX_IMAGENES = 5;
    private static final String CARPETA_IMAGENES = "propiedades";

    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final FileStorageService fileStorageService;

    public GestionPropiedadServiceImpl(
            PropiedadRepository propiedadRepository,
            UsuarioRepository usuarioRepository,
            ServicioRepository servicioRepository,
            FileStorageService fileStorageService) {
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicioRepository = servicioRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public PropiedadResponse publicarPropiedad(
            Long idArrendador, PropiedadRequest request, List<MultipartFile> imagenes) {
        Usuario arrendador = usuarioRepository.findById(idArrendador)
                .orElseThrow(() -> new RecursoNoEncontradoException("Arrendador no encontrado"));
        validarParejaCoordenadas(request);
        List<MultipartFile> archivos = imagenesConContenido(imagenes);
        List<Servicio> serviciosIncluidos = serviciosValidos(request.getServiciosIds());

        Propiedad propiedad = nuevaPropiedad(arrendador, request);
        serviciosIncluidos.forEach(propiedad::incluirServicio);

        List<String> imagenesSubidas = new ArrayList<>();
        try {
            short orden = 1;
            for (MultipartFile archivo : archivos) {
                String ruta = fileStorageService.storeImage(archivo, CARPETA_IMAGENES);
                imagenesSubidas.add(ruta);
                propiedad.addFotografia(ruta, orden++);
            }
            Propiedad guardada = propiedadRepository.saveAndFlush(propiedad);
            limpiarImagenesSiHayRollback(imagenesSubidas);
            return PropiedadMapper.toResponse(guardada, false);
        } catch (RuntimeException exception) {
            imagenesSubidas.forEach(fileStorageService::deleteFile);
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropiedadResponse> obtenerMisPropiedades(Long idArrendador) {
        return propiedadRepository
                .findByArrendador_IdUsuarioAndEstadoNotOrderByFechaPublicacionDesc(
                        idArrendador, Propiedad.ESTADO_ELIMINADA)
                .stream()
                .map(propiedad -> PropiedadMapper.toResponse(propiedad, false))
                .toList();
    }

    @Override
    @Transactional
    public PropiedadResponse cambiarEstado(Long idArrendador, Long idPropiedad, String nuevoEstado) {
        Propiedad propiedad = obtenerPropiedadDelArrendador(idArrendador, idPropiedad);
        propiedad.cambiarEstado(nuevoEstado);
        return PropiedadMapper.toResponse(propiedadRepository.save(propiedad), false);
    }

    @Override
    @Transactional
    public void eliminarPropiedad(Long idArrendador, Long idPropiedad) {
        // Baja lógica: el historial de conversaciones y arrendamientos se conserva.
        Propiedad propiedad = obtenerPropiedadDelArrendador(idArrendador, idPropiedad);
        propiedad.marcarEliminada();
        propiedadRepository.save(propiedad);
    }

    private Propiedad obtenerPropiedadDelArrendador(Long idArrendador, Long idPropiedad) {
        return propiedadRepository.findById(idPropiedad)
                .filter(propiedad -> propiedad.perteneceA(idArrendador))
                .filter(propiedad -> !propiedad.estaEliminada())
                .orElseThrow(() -> new RecursoNoEncontradoException("La propiedad no existe."));
    }

    private Propiedad nuevaPropiedad(Usuario arrendador, PropiedadRequest request) {
        Propiedad propiedad = new Propiedad();
        propiedad.setArrendador(arrendador);
        propiedad.setTitulo(request.getTitulo().trim());
        propiedad.setDescripcion(request.getDescripcion().trim());
        propiedad.setPrecioMensual(request.getPrecioMensual());
        propiedad.setCalle(request.getCalle().trim());
        propiedad.setNumeroExterior(request.getNumeroExterior().trim());
        propiedad.setNumeroInterior(textoOpcional(request.getNumeroInterior()));
        propiedad.setColonia(request.getColonia().trim());
        propiedad.setMunicipio(request.getMunicipio().trim());
        propiedad.setEstadoUbicacion(request.getEstadoUbicacion().trim());
        propiedad.setCodigoPostal(request.getCodigoPostal());
        propiedad.setLatitud(request.getLatitud());
        propiedad.setLongitud(request.getLongitud());
        propiedad.setHabitaciones(request.getHabitaciones());
        propiedad.setBanos(request.getBanos());
        propiedad.setEstacionamientos(request.getEstacionamientos());
        propiedad.setMetrosCuadrados(request.getMetrosCuadrados());
        return propiedad;
    }

    private List<MultipartFile> imagenesConContenido(List<MultipartFile> imagenes) {
        List<MultipartFile> archivos = imagenes == null ? List.of()
                : imagenes.stream().filter(archivo -> archivo != null && !archivo.isEmpty()).toList();
        if (archivos.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos una fotografía de la propiedad.");
        }
        if (archivos.size() > MAX_IMAGENES) {
            throw new IllegalArgumentException("No se pueden subir más de " + MAX_IMAGENES + " imágenes.");
        }
        return archivos;
    }

    private List<Servicio> serviciosValidos(List<Long> serviciosIds) {
        if (serviciosIds == null || serviciosIds.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = new LinkedHashSet<>(serviciosIds);
        List<Servicio> servicios = servicioRepository.findAllById(ids);
        if (servicios.size() != ids.size()
                || servicios.stream().anyMatch(servicio -> !"activo".equals(servicio.getEstado()))) {
            throw new IllegalArgumentException("Los servicios incluidos deben existir y estar activos.");
        }
        return servicios;
    }

    private void validarParejaCoordenadas(PropiedadRequest request) {
        if ((request.getLatitud() == null) != (request.getLongitud() == null)) {
            throw new IllegalArgumentException("La latitud y la longitud deben enviarse juntas.");
        }
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void limpiarImagenesSiHayRollback(List<String> rutas) {
        if (rutas.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        List<String> rutasSubidas = List.copyOf(rutas);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    rutasSubidas.forEach(fileStorageService::deleteFile);
                }
            }
        });
    }
}
