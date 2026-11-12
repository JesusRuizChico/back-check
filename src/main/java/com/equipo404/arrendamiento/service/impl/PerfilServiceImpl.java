package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.dto.request.ActualizarPerfilRequest;
import com.equipo404.arrendamiento.dto.request.CambiarContrasenaRequest;
import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.exception.RecursoNoEncontradoException;
import com.equipo404.arrendamiento.mapper.UsuarioMapper;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRolRepository;
import com.equipo404.arrendamiento.service.FileStorageService;
import com.equipo404.arrendamiento.service.PerfilService;
import java.nio.charset.StandardCharsets;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PerfilServiceImpl implements PerfilService {

    private static final String CARPETA_FOTOS = "perfiles";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public PerfilServiceImpl(
            UsuarioRepository usuarioRepository,
            UsuarioRolRepository usuarioRolRepository,
            FileStorageService fileStorageService,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerMiPerfil(Long idUsuario) {
        return respuesta(obtenerUsuarioActivo(idUsuario));
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarDatos(Long idUsuario, ActualizarPerfilRequest request) {
        Usuario usuario = obtenerUsuarioActivo(idUsuario);
        usuario.setNombre(request.nombre().trim());
        usuario.setTelefono(textoOpcional(request.telefono()));
        usuario.setBiografia(textoOpcional(request.biografia()));
        return respuesta(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void cambiarContrasena(Long idUsuario, CambiarContrasenaRequest request) {
        Usuario usuario = obtenerUsuarioActivo(idUsuario);
        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (request.nuevaContrasena().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar 72 bytes UTF-8");
        }
        usuario.setPasswordHash(passwordEncoder.encode(request.nuevaContrasena()));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarFotoPerfil(Long idUsuario, MultipartFile archivo) {
        Usuario usuario = obtenerUsuarioActivo(idUsuario);
        String fotoAnterior = usuario.getFotoPerfil();
        String rutaNueva = fileStorageService.storeImage(archivo, CARPETA_FOTOS);
        try {
            usuario.setFotoPerfil(rutaNueva);
            usuarioRepository.save(usuario);
            UsuarioResponse response = respuesta(usuario);
            limpiarImagenSegunResultadoTransaccion(fotoAnterior, rutaNueva);
            return response;
        } catch (RuntimeException exception) {
            fileStorageService.deleteFile(rutaNueva);
            throw exception;
        }
    }

    @Override
    @Transactional
    public UsuarioResponse eliminarFotoPerfil(Long idUsuario) {
        Usuario usuario = obtenerUsuarioActivo(idUsuario);
        String fotoAnterior = usuario.getFotoPerfil();
        usuario.setFotoPerfil(null);
        usuarioRepository.save(usuario);
        UsuarioResponse response = respuesta(usuario);
        limpiarImagenSegunResultadoTransaccion(fotoAnterior, null);
        return response;
    }

    private Usuario obtenerUsuarioActivo(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!usuario.estaActivo()) {
            throw new DisabledException("La cuenta no está activa");
        }
        return usuario;
    }

    private UsuarioResponse respuesta(Usuario usuario) {
        return UsuarioMapper.toResponse(usuario, usuarioRolRepository.findByUsuario(usuario));
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    /** Borra la foto anterior solo si la transacción se confirma; si se revierte, borra la nueva. */
    private void limpiarImagenSegunResultadoTransaccion(String eliminarAlConfirmar, String eliminarSiRevierte) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            fileStorageService.deleteFile(eliminarAlConfirmar);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                fileStorageService.deleteFile(status == STATUS_COMMITTED ? eliminarAlConfirmar : eliminarSiRevierte);
            }
        });
    }
}
