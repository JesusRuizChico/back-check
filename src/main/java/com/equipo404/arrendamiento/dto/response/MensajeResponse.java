package com.equipo404.arrendamiento.dto.response;

import java.time.OffsetDateTime;

public record MensajeResponse(
        Long idMensaje,
        Long idEmisor,
        String nombreEmisor,
        String contenido,
        OffsetDateTime fechaEnvio,
        OffsetDateTime fechaLectura) {
}
