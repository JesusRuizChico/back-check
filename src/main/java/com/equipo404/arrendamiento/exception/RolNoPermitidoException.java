package com.equipo404.arrendamiento.exception;

public class RolNoPermitidoException extends RuntimeException {

    public RolNoPermitidoException() {
        super("El rol solicitado no está permitido para registro público.");
    }

    public RolNoPermitidoException(String mensaje) {
        super(mensaje);
    }
}