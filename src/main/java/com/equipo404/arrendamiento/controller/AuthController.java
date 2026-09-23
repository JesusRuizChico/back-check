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
import java.time.OffsetDateTime;
import com.equipo404.arrendamiento.exception.CuentaBloqueadaException;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;

@RestController
public class AuthController {
    private final RegistroService registroService;
    private final PerfilService perfilService;
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy loginSessionStrategy;
    private final SecurityContextRepository contextRepository;
    private final UsuarioRepository usuarioRepository;

    public AuthController(RegistroService registroService, PerfilService perfilService,
                          AuthenticationManager authenticationManager,
                          SessionAuthenticationStrategy loginSessionStrategy,
                          SecurityContextRepository contextRepository,
                          UsuarioRepository usuarioRepository) {
        this.registroService = registroService;
        this.perfilService = perfilService;
        this.authenticationManager = authenticationManager;
        this.loginSessionStrategy = loginSessionStrategy;
        this.contextRepository = contextRepository;
        this.usuarioRepository = usuarioRepository;
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
        
        Usuario usuarioEntity = usuarioRepository.findByCorreo(login.getCorreo()).orElse(null);
        if (usuarioEntity != null && usuarioEntity.getBloqueadoHasta() != null) {
            if (usuarioEntity.getBloqueadoHasta().isAfter(OffsetDateTime.now())) {
                throw new CuentaBloqueadaException("Cuenta bloqueada. Intente de nuevo más tarde.");
            } else {
                usuarioEntity.setIntentosFallidos(0);
                usuarioEntity.setBloqueadoHasta(null);
                usuarioRepository.save(usuarioEntity);
            }
        }

        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            login.getCorreo(), login.getContrasena()));
            var principal = (UsuarioPrincipal) authentication.getPrincipal();
            UsuarioResponse perfil = perfilService.obtenerMiPerfil(principal.getIdUsuario());
            perfilService.registrarAcceso(principal.getIdUsuario());

            if (usuarioEntity != null && usuarioEntity.getIntentosFallidos() > 0) {
                usuarioEntity.setIntentosFallidos(0);
                usuarioEntity.setBloqueadoHasta(null);
                usuarioRepository.save(usuarioEntity);
            }

            // El login JSON debe rotar la sesión y persistir explícitamente el contexto.
            loginSessionStrategy.onAuthentication(authentication, request, response);
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response);
            return perfil;
        } catch (BadCredentialsException e) {
            if (usuarioEntity != null) {
                int intentos = usuarioEntity.getIntentosFallidos() + 1;
                usuarioEntity.setIntentosFallidos(intentos);
                if (intentos >= 5) {
                    usuarioEntity.setBloqueadoHasta(OffsetDateTime.now().plusMinutes(5));
                    usuarioRepository.save(usuarioEntity);
                    throw new CuentaBloqueadaException("Cuenta bloqueada por 5 minutos.");
                }
                usuarioRepository.save(usuarioEntity);
            }
            throw e;
        }
    }
}
