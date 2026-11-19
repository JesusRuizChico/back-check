package com.equipo404.arrendamiento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @Pattern(regexp = "^$|^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
        String telefono,

        @Size(max = 500, message = "La biografía no puede superar 500 caracteres")
        String biografia) {
}
