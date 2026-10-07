package com.equipo404.arrendamiento.dto.response;

import java.time.OffsetDateTime;

public record InicioConversacionResponse(
        Long idConversacion,
        boolean conversacionNueva,
        UsuarioChatResponse arrendador,
        MensajeResponse mensajeInicial,
        boolean avisoRespuestaLenta,
        OffsetDateTime ultimoAccesoArrendador) {
}
