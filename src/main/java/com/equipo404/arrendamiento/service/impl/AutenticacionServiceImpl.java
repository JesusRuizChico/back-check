package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.dto.request.LoginRequest;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.AutenticacionService;
import com.equipo404.arrendamiento.service.ControlAccesoService;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionServiceImpl implements AutenticacionService {

    private final AuthenticationManager authenticationManager;
    private final ControlAccesoService controlAccesoService;

    public AutenticacionServiceImpl(
            AuthenticationManager authenticationManager,
            ControlAccesoService controlAccesoService) {
        this.authenticationManager = authenticationManager;
        this.controlAccesoService = controlAccesoService;
    }

    @Override
    public Authentication autenticar(LoginRequest request) {
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);
        controlAccesoService.verificarCuentaNoBloqueada(correo);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(correo, request.getContrasena()));
            UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
            controlAccesoService.registrarAccesoExitoso(principal.getIdUsuario());
            return authentication;
        } catch (BadCredentialsException exception) {
            controlAccesoService.registrarIntentoFallido(correo);
            throw exception;
        }
    }
}
