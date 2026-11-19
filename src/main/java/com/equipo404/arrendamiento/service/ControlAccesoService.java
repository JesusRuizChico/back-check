package com.equipo404.arrendamiento.service;

public interface ControlAccesoService {

    void verificarCuentaNoBloqueada(String correo);

    void registrarIntentoFallido(String correo);

    void registrarAccesoExitoso(Long idUsuario);
}
