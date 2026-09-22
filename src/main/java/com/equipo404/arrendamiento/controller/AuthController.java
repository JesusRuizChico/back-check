package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.request.LoginRequest;
import com.equipo404.arrendamiento.dto.request.RegistroRequest;
import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.PerfilService;
import com.equipo404.arrendamiento.service.RegistroService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {
    private final RegistroService registroService;
    private final PerfilService perfilService;
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy loginSessionStrategy;
    private final SecurityContextRepository contextRepository;

    public AuthController(RegistroService registroService, PerfilService perfilService,
                          AuthenticationManager authenticationManager,
                          SessionAuthenticationStrategy loginSessionStrategy,
                          SecurityContextRepository contextRepository) {
        this.registroService = registroService;
        this.perfilService = perfilService;
        this.authenticationManager = authenticationManager;
        this.loginSessionStrategy = loginSessionStrategy;
        this.contextRepository = contextRepository;
    }

    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken(), "headerName", csrfToken.getHeaderName());
    }

    @PostMapping("/api/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest registro) {
        return registroService.registrar(registro);
    }

    @PostMapping("/api/auth/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest login,
                                 HttpServletRequest request, HttpServletResponse response) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        login.getCorreo(), login.getContrasena()));
        var principal = (UsuarioPrincipal) authentication.getPrincipal();
        UsuarioResponse perfil = perfilService.obtenerMiPerfil(principal.getIdUsuario());
        perfilService.registrarAcceso(principal.getIdUsuario());

        // El login JSON debe rotar la sesión y persistir explícitamente el contexto.
        loginSessionStrategy.onAuthentication(authentication, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return perfil;
    }
}
