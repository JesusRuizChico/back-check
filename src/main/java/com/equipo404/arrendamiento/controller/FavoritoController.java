package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.FavoritoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favoritos")
@PreAuthorize("hasRole('ARRENDATARIO')")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    public List<PropiedadResponse> listar(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return favoritoService.listar(principal.getIdUsuario());
    }

    @PutMapping("/{idPropiedad}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void agregar(@PathVariable Long idPropiedad, @AuthenticationPrincipal UsuarioPrincipal principal) {
        favoritoService.agregar(principal.getIdUsuario(), idPropiedad);
    }

    @DeleteMapping("/{idPropiedad}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@PathVariable Long idPropiedad, @AuthenticationPrincipal UsuarioPrincipal principal) {
        favoritoService.quitar(principal.getIdUsuario(), idPropiedad);
    }
}
