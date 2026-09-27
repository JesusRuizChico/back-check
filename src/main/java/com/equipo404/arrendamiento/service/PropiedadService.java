package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.PropiedadRequest;
import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.entity.Propiedad;
import com.equipo404.arrendamiento.entity.PropiedadFotografia;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.repository.PropiedadRepository;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;

    public PropiedadService(PropiedadRepository propiedadRepository, 
                            UsuarioRepository usuarioRepository, 
                            FileStorageService fileStorageService) {
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public PropiedadResponse publicarPropiedad(Long idArrendador, PropiedadRequest request, List<MultipartFile> imagenes) {
        Usuario arrendador = usuarioRepository.findById(idArrendador)
                .orElseThrow(() -> new IllegalArgumentException("Arrendador no encontrado"));

        validarParejaCoordenadas(request);

        if (imagenes != null && imagenes.size() > 5) {
            throw new IllegalArgumentException("No se pueden subir más de 5 imágenes");
        }

        Propiedad propiedad = new Propiedad();
        propiedad.setArrendador(arrendador);
        propiedad.setTitulo(request.getTitulo());
        propiedad.setDescripcion(request.getDescripcion());
        propiedad.setPrecioMensual(request.getPrecioMensual());
        propiedad.setCalle(request.getCalle());
        propiedad.setNumeroExterior(request.getNumeroExterior());
        propiedad.setNumeroInterior(request.getNumeroInterior());
        propiedad.setColonia(request.getColonia());
        propiedad.setMunicipio(request.getMunicipio());
        propiedad.setEstadoUbicacion(request.getEstadoUbicacion());
        propiedad.setCodigoPostal(request.getCodigoPostal());
        propiedad.setLatitud(request.getLatitud());
        propiedad.setLongitud(request.getLongitud());
        
        List<String> imagenesSubidas = new ArrayList<>();
        try {
            if (imagenes != null) {
                short orden = 1;
                for (MultipartFile file : imagenes) {
                    if (file != null && !file.isEmpty()) {
                        String url = fileStorageService.storeImage(file, "propiedades");
                        imagenesSubidas.add(url);
                        propiedad.addFotografia(url, orden++);
                    }
                }
            }

            Propiedad guardada = propiedadRepository.save(propiedad);
            limpiarImagenesSiHayRollback(imagenesSubidas);
            return mapToResponse(guardada);
        } catch (RuntimeException exception) {
            imagenesSubidas.forEach(fileStorageService::deleteFile);
            throw exception;
        }
    }

    private void validarParejaCoordenadas(PropiedadRequest request) {
        boolean tieneLatitud = request.getLatitud() != null;
        boolean tieneLongitud = request.getLongitud() != null;
        if (tieneLatitud != tieneLongitud) {
            throw new IllegalArgumentException("La latitud y la longitud deben enviarse juntas.");
        }
    }

    private void limpiarImagenesSiHayRollback(List<String> urls) {
        if (urls.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        List<String> urlsSubidas = List.copyOf(urls);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    urlsSubidas.forEach(fileStorageService::deleteFile);
                }
            }
        });
    }

    @Transactional(readOnly = true)
    public List<PropiedadResponse> obtenerMisPropiedades(Long idArrendador) {
        List<Propiedad> propiedades = propiedadRepository.findByArrendador_IdUsuario(idArrendador);
        return propiedades.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PropiedadResponse mapToResponse(Propiedad propiedad) {
        PropiedadResponse response = new PropiedadResponse();
        response.setIdPropiedad(propiedad.getIdPropiedad());
        response.setTitulo(propiedad.getTitulo());
        response.setDescripcion(propiedad.getDescripcion());
        response.setPrecioMensual(propiedad.getPrecioMensual());
        response.setCalle(propiedad.getCalle());
        response.setNumeroExterior(propiedad.getNumeroExterior());
        response.setNumeroInterior(propiedad.getNumeroInterior());
        response.setColonia(propiedad.getColonia());
        response.setMunicipio(propiedad.getMunicipio());
        response.setEstadoUbicacion(propiedad.getEstadoUbicacion());
        response.setCodigoPostal(propiedad.getCodigoPostal());
        response.setLatitud(propiedad.getLatitud());
        response.setLongitud(propiedad.getLongitud());
        response.setEstado(propiedad.getEstado());
        response.setVerificada(propiedad.getVerificada());
        response.setFechaPublicacion(propiedad.getFechaPublicacion());
        
        List<String> imageUrls = propiedad.getFotografias().stream()
                .map(PropiedadFotografia::getUrl)
                .collect(Collectors.toList());
        response.setImagenes(imageUrls);
        
        List<String> servicios = propiedad.getServicios().stream()
                .map(ps -> ps.getServicio().getNombre())
                .collect(Collectors.toList());
        response.setServicios(servicios);
        
        return response;
    }
}
