package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.PerfilService;
import com.equipo404.arrendamiento.dto.request.ActualizarContactoRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public UsuarioResponse miPerfil(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return perfilService.obtenerMiPerfil(principal.getIdUsuario());
    }

    @PutMapping
    public UsuarioResponse actualizarContacto(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody ActualizarContactoRequest request) {

        return perfilService.actualizarContacto(principal.getIdUsuario(),
                request);
    }
}
