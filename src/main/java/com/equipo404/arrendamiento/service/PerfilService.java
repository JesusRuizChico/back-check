package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.dto.request.ActualizarContactoRequest;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.entity.UsuarioRol;
import com.equipo404.arrendamiento.mapper.UsuarioMapper;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRolRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.DisabledException;
import java.time.OffsetDateTime;

import java.util.List;

@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public PerfilService(
            UsuarioRepository usuarioRepository,
            UsuarioRolRepository usuarioRolRepository) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerMiPerfil(Long idUsuario) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!"activo".equals(usuario.getEstado())) {
            throw new DisabledException("La cuenta no está activa");
        }

        List<UsuarioRol> roles = usuarioRolRepository.findByUsuario(usuario);

        return UsuarioMapper.toResponse(usuario, roles);
    }

    @Transactional
    public UsuarioResponse actualizarContacto(Long idUsuario, ActualizarContactoRequest request) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!"activo".equals(usuario.getEstado())) {
            throw new DisabledException("La cuenta no está activa");
        }

        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }

        if (request.getCorreoAlterno() != null) {
            usuario.setCorreoAlterno(request.getCorreoAlterno());
        }

        usuario = usuarioRepository.save(usuario);

        List<UsuarioRol> roles = usuarioRolRepository.findByUsuario(usuario);

        return UsuarioMapper.toResponse(usuario, roles);
    }

    @Transactional
    public void registrarAcceso(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setUltimoAcceso(OffsetDateTime.now());
    }
}

