package com.equipo404.arrendamiento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnviarMensajeRequest(
        @NotBlank(message = "Escribe un mensaje antes de enviarlo.")
        @Size(max = 4000, message = "El mensaje no puede superar los 4000 caracteres.")
        String contenido) {
}
