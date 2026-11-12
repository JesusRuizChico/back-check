package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.LoginRequest;
import org.springframework.security.core.Authentication;

public interface AutenticacionService {

    /**
     * Verifica las credenciales aplicando el bloqueo por intentos fallidos.
     * La persistencia de la sesión HTTP es responsabilidad de la capa web.
     */
    Authentication autenticar(LoginRequest request);
}
