package com.equipo404.arrendamiento.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

/** Asocia una autenticación exitosa con la sesión HTTP del cliente. */
public interface GestorSesionWeb {

    void iniciarSesion(Authentication authentication, HttpServletRequest request, HttpServletResponse response);
}
