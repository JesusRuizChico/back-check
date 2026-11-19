package com.equipo404.arrendamiento.service;

/** Verificación de pertenencia usada por el canal en tiempo real del chat. */
public interface AccesoConversacionService {

    void validarParticipante(Long idConversacion, Long idUsuario);
}
