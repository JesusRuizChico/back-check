package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.PropiedadRequest;
import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.entity.Propiedad;
import com.equipo404.arrendamiento.entity.PropiedadFotografia;
import com.equipo404.arrendamiento.entity.PropiedadServicio;
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
    public PropiedadResponse publicarPropiedad(Long idPropietario, PropiedadRequest request, List<MultipartFile> imagenes) {
        Usuario propietario = usuarioRepository.findById(idPropietario)
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));

        if (imagenes != null && imagenes.size() > 5) {
            throw new IllegalArgumentException("No se pueden subir más de 5 imágenes");
        }

        Propiedad propiedad = new Propiedad();
        propiedad.setPropietario(propietario);
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
        
        // TODO: Handle request.getServiciosIds() mapping to PropiedadServicio if needed
        
        if (imagenes != null) {
            short orden = 1;
            for (MultipartFile file : imagenes) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);
                    
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/uploads/")
                            .path(fileName)
                            .toUriString();
                    
                    propiedad.addFotografia(fileDownloadUri, orden++);
                }
            }
        }

        Propiedad guardada = propiedadRepository.save(propiedad);
        return mapToResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<PropiedadResponse> obtenerMisPropiedades(Long idPropietario) {
        List<Propiedad> propiedades = propiedadRepository.findByPropietario_IdUsuario(idPropietario);
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
