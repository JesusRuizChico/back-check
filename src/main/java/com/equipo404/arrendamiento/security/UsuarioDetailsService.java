package com.equipo404.arrendamiento.security;

import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.entity.UsuarioRol;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRolRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public UsuarioDetailsService(
            UsuarioRepository usuarioRepository,
            UsuarioRolRepository usuarioRolRepository) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        String correoNormalizado =
                correo.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByCorreo(correoNormalizado)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        List<UsuarioRol> usuarioRoles =
                usuarioRolRepository.findByUsuario(usuario);

        List<GrantedAuthority> authorities = usuarioRoles.stream()
                .filter(usuarioRol ->
                        "activo".equals(usuarioRol.getEstado()))
                .<GrantedAuthority>map(usuarioRol ->
                        new SimpleGrantedAuthority(
                                "ROLE_" +
                                usuarioRol.getRol()
                                        .getNombre()
                                        .toUpperCase(Locale.ROOT)
                        )
                )
                .toList();

        return new UsuarioPrincipal(
                usuario.getIdUsuario(),
                usuario.getCorreo(),
                usuario.getPasswordHash(),
                usuario.getEstado(),
                authorities
        );
    }
}