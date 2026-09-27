package com.equipo404.arrendamiento.dto.response;

import java.time.OffsetDateTime;

public record ResumenConversacionResponse(
        Long idConversacion,
        UsuarioChatResponse otroParticipante,
        MensajeResponse ultimoMensaje,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaUltimoMensaje,
        long mensajesNoLeidos,
        boolean avisoRespuestaLenta) {
}
