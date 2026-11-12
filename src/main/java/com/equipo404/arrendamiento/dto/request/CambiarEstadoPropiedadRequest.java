package com.equipo404.arrendamiento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarEstadoPropiedadRequest(
        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "disponible|pausada|rentada",
                message = "El estado debe ser disponible, pausada o rentada")
        String estado) {
}
