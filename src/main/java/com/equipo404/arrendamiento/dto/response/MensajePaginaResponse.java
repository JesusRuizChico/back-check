package com.equipo404.arrendamiento.dto.response;

import java.util.List;

public record MensajePaginaResponse(
        List<MensajeResponse> mensajes,
        Long siguienteAntesDe,
        boolean hayMas) {
}
