package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.dto.request.RegistroRequest;
import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.entity.Rol;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.entity.UsuarioRol;
import com.equipo404.arrendamiento.exception.CorreoYaRegistradoException;
import com.equipo404.arrendamiento.exception.RolNoPermitidoException;
import com.equipo404.arrendamiento.mapper.UsuarioMapper;
import com.equipo404.arrendamiento.repository.RolRepository;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRolRepository;
import com.equipo404.arrendamiento.service.RegistroService;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroServiceImpl implements RegistroService {

    /** Perfiles disponibles en el registro público: inquilino y arrendador. */
    private static final Set<String> ROLES_REGISTRO_PUBLICO = Set.of("arrendatario", "arrendador");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String nombre = request.getNombre().trim();
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);
        String contrasena = request.getContrasena();
        String rolSolicitado = request.getRolSolicitado().trim().toLowerCase(Locale.ROOT);
        String telefono = request.getTelefono() == null || request.getTelefono().isBlank()
                ? null : request.getTelefono().trim();

        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        // BCrypt admite como máximo 72 bytes, no 72 caracteres Unicode.
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar 72 bytes UTF-8");
        }
        if (!ROLES_REGISTRO_PUBLICO.contains(rolSolicitado)) {
            throw new RolNoPermitidoException();
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new CorreoYaRegistradoException();
        }

        Rol rol = rolRepository.findByNombre(rolSolicitado)
                .orElseThrow(() -> new IllegalStateException("El rol solicitado no existe en la base de datos"));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(contrasena));
        usuario.setTelefono(telefono);
        usuario.setEstado("activo");
        usuario = usuarioRepository.save(usuario);

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);
        usuarioRol.setFechaAsignacion(OffsetDateTime.now());
        usuarioRolRepository.save(usuarioRol);

        return UsuarioMapper.toResponse(usuario, List.of(usuarioRol));
    }
}
