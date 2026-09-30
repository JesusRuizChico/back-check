package com.equipo404.arrendamiento.dto.response;

public record ConversacionPropiedadResponse(
        Long idConversacion,
        UsuarioChatResponse arrendador) {
}
