package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.PerfilService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(value = "/foto", consumes = {"multipart/form-data"})
    public ResponseEntity<UsuarioResponse> actualizarFotoPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) {

        MultipartFile archivoFinal = (foto != null && !foto.isEmpty()) ? foto
                : (imagen != null && !imagen.isEmpty()) ? imagen
                : archivo;

        if (archivoFinal == null || archivoFinal.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un archivo de imagen válido.");
        }

        UsuarioResponse response = perfilService.actualizarFotoPerfil(principal.getIdUsuario(), archivoFinal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/foto")
    public ResponseEntity<UsuarioResponse> eliminarFotoPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        UsuarioResponse response = perfilService.eliminarFotoPerfil(principal.getIdUsuario());
        return ResponseEntity.ok(response);
    }
}
