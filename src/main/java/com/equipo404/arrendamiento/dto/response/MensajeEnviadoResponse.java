package com.equipo404.arrendamiento.dto.response;

import java.time.OffsetDateTime;

public record MensajeEnviadoResponse(
        MensajeResponse mensaje,
        boolean avisoRespuestaLenta,
        OffsetDateTime ultimoAccesoArrendador) {
}
