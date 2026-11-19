package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.response.MensajeResponse;
import java.time.OffsetDateTime;

/** Publica los eventos de una conversación a los clientes suscritos. */
public interface NotificadorConversacion {

    void notificarMensaje(Long idConversacion, MensajeResponse mensaje, Long idEmisor, boolean avisoRespuestaLenta);

    void notificarLectura(Long idConversacion, Long idUsuario, OffsetDateTime fechaLectura);
}
