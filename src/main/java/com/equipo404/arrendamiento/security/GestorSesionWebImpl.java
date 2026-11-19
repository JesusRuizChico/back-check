package com.equipo404.arrendamiento.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class GestorSesionWebImpl implements GestorSesionWeb {

    private final SessionAuthenticationStrategy loginSessionStrategy;
    private final SecurityContextRepository contextRepository;

    public GestorSesionWebImpl(
            SessionAuthenticationStrategy loginSessionStrategy,
            SecurityContextRepository contextRepository) {
        this.loginSessionStrategy = loginSessionStrategy;
        this.contextRepository = contextRepository;
    }

    @Override
    public void iniciarSesion(Authentication authentication, HttpServletRequest request,
                              HttpServletResponse response) {
        // El login JSON debe rotar la sesión y persistir explícitamente el contexto.
        loginSessionStrategy.onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
    }
}
