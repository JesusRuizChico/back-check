package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.request.CambiarEstadoPropiedadRequest;
import com.equipo404.arrendamiento.dto.request.PropiedadRequest;
import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.CatalogoPropiedadService;
import com.equipo404.arrendamiento.service.GestionPropiedadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/propiedades")
public class PropiedadController {

    private final CatalogoPropiedadService catalogoPropiedadService;
    private final GestionPropiedadService gestionPropiedadService;

    public PropiedadController(
            CatalogoPropiedadService catalogoPropiedadService,
            GestionPropiedadService gestionPropiedadService) {
        this.catalogoPropiedadService = catalogoPropiedadService;
        this.gestionPropiedadService = gestionPropiedadService;
    }

    @GetMapping
    public List<PropiedadResponse> obtenerCatalogoDisponible(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return catalogoPropiedadService.obtenerCatalogoDisponible(principal.getIdUsuario());
    }

    @GetMapping("/{idPropiedad}")
    public PropiedadResponse obtenerDetalle(
            @PathVariable Long idPropiedad,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return catalogoPropiedadService.obtenerDetalle(idPropiedad, principal.getIdUsuario());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARRENDADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public PropiedadResponse publicarPropiedad(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @ModelAttribute PropiedadRequest request,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        return gestionPropiedadService.publicarPropiedad(principal.getIdUsuario(), request, imagenes);
    }

    @GetMapping("/mis-propiedades")
    @PreAuthorize("hasRole('ARRENDADOR')")
    public List<PropiedadResponse> obtenerMisPropiedades(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return gestionPropiedadService.obtenerMisPropiedades(principal.getIdUsuario());
    }

    @PatchMapping("/{idPropiedad}/estado")
    @PreAuthorize("hasRole('ARRENDADOR')")
    public PropiedadResponse cambiarEstado(
            @PathVariable Long idPropiedad,
            @Valid @RequestBody CambiarEstadoPropiedadRequest request,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return gestionPropiedadService.cambiarEstado(principal.getIdUsuario(), idPropiedad, request.estado());
    }

    @DeleteMapping("/{idPropiedad}")
    @PreAuthorize("hasRole('ARRENDADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPropiedad(
            @PathVariable Long idPropiedad,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        gestionPropiedadService.eliminarPropiedad(principal.getIdUsuario(), idPropiedad);
    }
}
