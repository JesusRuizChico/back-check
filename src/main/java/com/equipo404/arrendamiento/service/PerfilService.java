package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.ActualizarPerfilRequest;
import com.equipo404.arrendamiento.dto.request.CambiarContrasenaRequest;
import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PerfilService {

    UsuarioResponse obtenerMiPerfil(Long idUsuario);

    UsuarioResponse actualizarDatos(Long idUsuario, ActualizarPerfilRequest request);

    void cambiarContrasena(Long idUsuario, CambiarContrasenaRequest request);

    UsuarioResponse actualizarFotoPerfil(Long idUsuario, MultipartFile archivo);

    UsuarioResponse eliminarFotoPerfil(Long idUsuario);
}
