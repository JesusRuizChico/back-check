package com.equipo404.arrendamiento.exception;

public class CorreoYaRegistradoException extends RuntimeException {

    public CorreoYaRegistradoException() {
        super("El correo ya se encuentra registrado.");
    }

    public CorreoYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}