package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.entity.UsuarioRol;
import com.equipo404.arrendamiento.mapper.UsuarioMapper;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRolRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.time.OffsetDateTime;

import java.util.List;

@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final FileStorageService fileStorageService;

    public PerfilService(
            UsuarioRepository usuarioRepository,
            UsuarioRolRepository usuarioRolRepository,
            FileStorageService fileStorageService) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerMiPerfil(Long idUsuario) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"
                        )
                );

        if (!"activo".equals(usuario.getEstado())) {
            throw new DisabledException("La cuenta no está activa");
        }

        List<UsuarioRol> roles =
                usuarioRolRepository.findByUsuario(usuario);

        return UsuarioMapper.toResponse(usuario, roles);
    }
    @Transactional
    public void registrarAcceso(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setUltimoAcceso(OffsetDateTime.now());
    }

    @Transactional
    public UsuarioResponse actualizarFotoPerfil(Long idUsuario, MultipartFile archivo) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!"activo".equals(usuario.getEstado())) {
            throw new DisabledException("La cuenta no está activa");
        }

        String fotoAnterior = usuario.getFotoPerfil();
        String nombreArchivo = fileStorageService.storeImage(archivo);

        String urlDescarga;
        try {
            urlDescarga = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(nombreArchivo)
                    .toUriString();
        } catch (IllegalStateException ex) {
            urlDescarga = "/uploads/" + nombreArchivo;
        }

        usuario.setFotoPerfil(urlDescarga);
        usuarioRepository.save(usuario);

        eliminarArchivoFisicoSiExiste(fotoAnterior);

        List<UsuarioRol> roles = usuarioRolRepository.findByUsuario(usuario);
        return UsuarioMapper.toResponse(usuario, roles);
    }

    @Transactional
    public UsuarioResponse eliminarFotoPerfil(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!"activo".equals(usuario.getEstado())) {
            throw new DisabledException("La cuenta no está activa");
        }

        String fotoAnterior = usuario.getFotoPerfil();
        usuario.setFotoPerfil(null);
        usuarioRepository.save(usuario);

        eliminarArchivoFisicoSiExiste(fotoAnterior);

        List<UsuarioRol> roles = usuarioRolRepository.findByUsuario(usuario);
        return UsuarioMapper.toResponse(usuario, roles);
    }

    private void eliminarArchivoFisicoSiExiste(String urlFoto) {
        if (urlFoto != null && urlFoto.contains("/uploads/")) {
            String nombreArchivo = urlFoto.substring(urlFoto.lastIndexOf("/uploads/") + "/uploads/".length());
            fileStorageService.deleteFile(nombreArchivo);
        }
    }
}