package com.equipo404.arrendamiento.mapper;

import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.entity.UsuarioRol;

import java.util.List;

public class UsuarioMapper {

    public static UsuarioResponse toResponse(
            Usuario usuario,
            List<UsuarioRol> usuarioRoles) {

        List<String> rolesActivos = usuarioRoles.stream()
                .filter(usuarioRol -> "activo".equals(usuarioRol.getEstado()))
                .map(usuarioRol -> usuarioRol.getRol().getNombre())
                .toList();

        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getFotoPerfil(),
                rolesActivos
        );
    }
}