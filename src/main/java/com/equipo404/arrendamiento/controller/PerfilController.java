package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.request.ActualizarPerfilRequest;
import com.equipo404.arrendamiento.dto.request.CambiarContrasenaRequest;
import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public UsuarioResponse actualizarDatos(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        return perfilService.actualizarDatos(principal.getIdUsuario(), request);
    }

    @PutMapping("/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody CambiarContrasenaRequest request) {
        perfilService.cambiarContrasena(principal.getIdUsuario(), request);
    }

    @PostMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse actualizarFotoPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestPart("foto") MultipartFile foto) {
        return perfilService.actualizarFotoPerfil(principal.getIdUsuario(), foto);
    }

    @DeleteMapping("/foto")
    public UsuarioResponse eliminarFotoPerfil(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return perfilService.eliminarFotoPerfil(principal.getIdUsuario());
    }
}
