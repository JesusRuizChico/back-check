package com.equipo404.arrendamiento.security;

import com.equipo404.arrendamiento.dto.response.ErrorResponse;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse error;

        if (accessDeniedException instanceof CsrfException) {

            error = new ErrorResponse(
                    "CSRF_INVALIDO",
                    "El token CSRF es inválido o no fue enviado.",
                    null
            );

        } else {

            error = new ErrorResponse(
                    "ACCESO_DENEGADO",
                    "No tienes permisos para realizar esta operación.",
                    null
            );
        }

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}