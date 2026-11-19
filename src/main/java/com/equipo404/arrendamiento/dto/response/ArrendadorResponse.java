package com.equipo404.arrendamiento.dto.response;

public record ArrendadorResponse(
        Long idUsuario,
        String nombre,
        String telefono,
        String fotoPerfil) {
}
