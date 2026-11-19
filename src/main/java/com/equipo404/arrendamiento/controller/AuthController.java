package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.request.LoginRequest;
import com.equipo404.arrendamiento.dto.request.RegistroRequest;
import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.security.GestorSesionWeb;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.AutenticacionService;
import com.equipo404.arrendamiento.service.PerfilService;
import com.equipo404.arrendamiento.service.RegistroService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistroService registroService;
    private final AutenticacionService autenticacionService;
    private final PerfilService perfilService;
    private final GestorSesionWeb gestorSesionWeb;

    public AuthController(
            RegistroService registroService,
            AutenticacionService autenticacionService,
            PerfilService perfilService,
            GestorSesionWeb gestorSesionWeb) {
        this.registroService = registroService;
        this.autenticacionService = autenticacionService;
        this.perfilService = perfilService;
        this.gestorSesionWeb = gestorSesionWeb;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest registro) {
        return registroService.registrar(registro);
    }

    @PostMapping("/login")
    public UsuarioResponse login(
            @Valid @RequestBody LoginRequest login,
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication = autenticacionService.autenticar(login);
        UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
        UsuarioResponse perfil = perfilService.obtenerMiPerfil(principal.getIdUsuario());
        gestorSesionWeb.iniciarSesion(authentication, request, response);
        return perfil;
    }
}
