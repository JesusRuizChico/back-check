package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.PropiedadRequest;
import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.entity.Propiedad;
import com.equipo404.arrendamiento.entity.PropiedadImagen;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.repository.PropiedadRepository;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

        if (imagenes != null && imagenes.size() > 5) {
            throw new IllegalArgumentException("No se pueden subir más de 5 imágenes");
        }

        Propiedad propiedad = new Propiedad();
        propiedad.setArrendador(arrendador);
        propiedad.setTitulo(request.getTitulo());
        propiedad.setDescripcion(request.getDescripcion());
        propiedad.setPrecio(request.getPrecio());
        propiedad.setUbicacion(request.getUbicacion());
        propiedad.setHabitaciones(request.getHabitaciones() != null ? request.getHabitaciones() : 1);
        propiedad.setServicios(request.getServicios());
        
        if (imagenes != null) {
            for (MultipartFile file : imagenes) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);
                    
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/uploads/")
                            .path(fileName)
                            .toUriString();
                    
                    propiedad.addImagen(fileDownloadUri);
                }
            }
        }

        Propiedad guardada = propiedadRepository.save(propiedad);
        return mapToResponse(guardada);
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
        response.setPrecio(propiedad.getPrecio());
        response.setUbicacion(propiedad.getUbicacion());
        response.setHabitaciones(propiedad.getHabitaciones());
        response.setServicios(propiedad.getServicios());
        response.setEstado(propiedad.getEstado());
        response.setFechaPublicacion(propiedad.getFechaPublicacion());
        
        List<String> imageUrls = propiedad.getImagenes().stream()
                .map(PropiedadImagen::getUrlImagen)
                .collect(Collectors.toList());
        response.setImagenes(imageUrls);
        
        return response;
    }
}
