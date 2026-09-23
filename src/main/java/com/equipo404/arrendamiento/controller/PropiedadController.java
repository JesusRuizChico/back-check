package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.request.PropiedadRequest;
import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.PropiedadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/propiedades")
public class PropiedadController {

    private final PropiedadService propiedadService;

    public PropiedadController(PropiedadService propiedadService) {
        this.propiedadService = propiedadService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('ROLE_ARRENDADOR')")
    public ResponseEntity<PropiedadResponse> publicarPropiedad(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @ModelAttribute PropiedadRequest request,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {

        PropiedadResponse response = propiedadService.publicarPropiedad(principal.getIdUsuario(), request, imagenes);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mis-propiedades")
    @PreAuthorize("hasAuthority('ROLE_ARRENDADOR')")
    public ResponseEntity<List<PropiedadResponse>> obtenerMisPropiedades(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        
        List<PropiedadResponse> propiedades = propiedadService.obtenerMisPropiedades(principal.getIdUsuario());
        return ResponseEntity.ok(propiedades);
    }
}
