package com.equipo404.arrendamiento.exception;

import org.springframework.security.core.AuthenticationException;

public class CuentaBloqueadaException extends AuthenticationException {
    public CuentaBloqueadaException(String msg) {
        super(msg);
    }
}
