package com.equipo404.arrendamiento.dto.response;

import java.time.OffsetDateTime;

public record EventoConversacionResponse(
        String tipo,
        MensajeResponse mensaje,
        Long idUsuario,
        OffsetDateTime fechaEvento,
        boolean avisoRespuestaLenta) {
}
