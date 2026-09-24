package com.equipo404.arrendamiento.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ActualizarContactoRequest {

    @Size(max = 25, message = "El teléfono no puede exceder los 25 caracteres")
    @Pattern(regexp = "^$|^[0-9+\\-\\s()]+$", message = "El teléfono contiene un formato inválido")
    private String telefono;

    @Email(message = "El correo alterno debe ser válido")
    @Size(max = 254, message = "El correo alterno no puede exceder los 254 caracteres")
    private String correoAlterno;

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoAlterno() {
        return correoAlterno;
    }

    public void setCorreoAlterno(String correoAlterno) {
        this.correoAlterno = correoAlterno;
    }
}

