package com.equipo404.arrendamiento.dto.response;

import java.time.OffsetDateTime;

public record LecturaResponse(
        int mensajesMarcados,
        OffsetDateTime fechaLectura) {
}
