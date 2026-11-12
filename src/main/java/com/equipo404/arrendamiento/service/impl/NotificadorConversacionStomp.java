package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.dto.response.EventoConversacionResponse;
import com.equipo404.arrendamiento.dto.response.MensajeResponse;
import com.equipo404.arrendamiento.service.NotificadorConversacion;
import java.time.OffsetDateTime;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Envía los eventos del chat al tópico STOMP de cada conversación. */
@Component
public class NotificadorConversacionStomp implements NotificadorConversacion {

    private static final String TOPICO = "/topic/conversaciones/";

    private final SimpMessagingTemplate messagingTemplate;

    public NotificadorConversacionStomp(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notificarMensaje(Long idConversacion, MensajeResponse mensaje, Long idEmisor,
                                 boolean avisoRespuestaLenta) {
        messagingTemplate.convertAndSend(TOPICO + idConversacion,
                new EventoConversacionResponse("MENSAJE", mensaje, idEmisor, mensaje.fechaEnvio(),
                        avisoRespuestaLenta));
    }

    @Override
    public void notificarLectura(Long idConversacion, Long idUsuario, OffsetDateTime fechaLectura) {
        messagingTemplate.convertAndSend(TOPICO + idConversacion,
                new EventoConversacionResponse("LECTURA", null, idUsuario, fechaLectura, false));
    }
}
