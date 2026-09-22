package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.RegistroRequest;
import com.equipo404.arrendamiento.exception.CorreoYaRegistradoException;
import com.equipo404.arrendamiento.exception.RolNoPermitidoException;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.entity.Rol;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.entity.UsuarioRol;
import com.equipo404.arrendamiento.mapper.UsuarioMapper;
import com.equipo404.arrendamiento.repository.RolRepository;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRolRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class RegistroService {

    private static final Set<String> ROLES_REGISTRO_PUBLICO = Set.of(
            "arrendatario",
            "arrendador",
            "proveedor"
    );

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {

        String nombre = request.getNombre().trim();
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);
        String contrasena = request.getContrasena();
        String rolSolicitado = request.getRolSolicitado().trim().toLowerCase(Locale.ROOT);

        String telefono = null;

        if (request.getTelefono() != null
                && !request.getTelefono().isBlank()) {

            telefono = request.getTelefono().trim();
        }

        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (correo.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }

        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        // BCrypt admite como máximo 72 bytes, no 72 caracteres Unicode.
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar 72 bytes UTF-8");
        }

        if (!ROLES_REGISTRO_PUBLICO.contains(rolSolicitado)) {
            throw new RolNoPermitidoException();
        }

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoYaRegistradoException();
        }

        Rol rol = rolRepository.findByNombre(rolSolicitado)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El rol solicitado no existe en la base de datos"
                        )
                );

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setPasswordHash(
                passwordEncoder.encode(contrasena)
        );
        usuario.setTelefono(telefono);
        usuario.setEstado("activo");

        usuario = usuarioRepository.save(usuario);

        UsuarioRol usuarioRol = new UsuarioRol();

        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);
        usuarioRol.setEstado("activo");
        usuarioRol.setFechaAsignacion(OffsetDateTime.now());

        usuarioRolRepository.save(usuarioRol);

        return UsuarioMapper.toResponse(
                usuario,
                List.of(usuarioRol)
        );
    }
}