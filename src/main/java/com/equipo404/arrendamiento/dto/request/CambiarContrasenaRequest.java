package com.equipo404.arrendamiento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest(
        @NotBlank(message = "Ingresa tu contraseña actual")
        @Size(max = 72)
        String contrasenaActual,

        @NotBlank(message = "Ingresa la nueva contraseña")
        @Size(min = 6, max = 72, message = "La nueva contraseña debe tener entre 6 y 72 caracteres")
        String nuevaContrasena) {
}
