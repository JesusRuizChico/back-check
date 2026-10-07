package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.request.EnviarMensajeRequest;
import com.equipo404.arrendamiento.dto.response.ErrorResponse;
import com.equipo404.arrendamiento.dto.response.EventoConversacionResponse;
import com.equipo404.arrendamiento.dto.response.MensajeEnviadoResponse;
import com.equipo404.arrendamiento.exception.ChatAccesoDenegadoException;
import com.equipo404.arrendamiento.exception.RecursoNoEncontradoException;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.ConversacionService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;

@org.springframework.stereotype.Controller
public class ConversacionWebSocketController {

    private final ConversacionService conversacionService;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversacionWebSocketController(
            ConversacionService conversacionService,
            SimpMessagingTemplate messagingTemplate) {
        this.conversacionService = conversacionService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/conversaciones/{idConversacion}/mensajes")
    public void enviar(
            @DestinationVariable Long idConversacion,
            @Valid @Payload EnviarMensajeRequest request,
            Principal principal) {
        Long idUsuario = obtenerIdUsuario(principal);
        MensajeEnviadoResponse respuesta = conversacionService.enviar(
                idConversacion, idUsuario, request);
        messagingTemplate.convertAndSend(
                "/topic/conversaciones/" + idConversacion,
                new EventoConversacionResponse(
                        "MENSAJE", respuesta.mensaje(), idUsuario,
                        respuesta.mensaje().fechaEnvio(), respuesta.avisoRespuestaLenta()));
    }

    @MessageExceptionHandler({
            ChatAccesoDenegadoException.class,
            RecursoNoEncontradoException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class
    })
    @SendToUser("/queue/errores")
    public ErrorResponse manejarErrorDeChat(Exception exception) {
        if (exception instanceof RecursoNoEncontradoException) {
            return new ErrorResponse("NO_ENCONTRADO", "La conversación no existe o no tienes acceso.");
        }
        if (exception instanceof ChatAccesoDenegadoException) {
            return new ErrorResponse("ACCESO_DENEGADO", exception.getMessage());
        }
        return new ErrorResponse("MENSAJE_INVALIDO", "El mensaje está vacío o supera los 4000 caracteres.");
    }

    private Long obtenerIdUsuario(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UsuarioPrincipal usuario) {
            return usuario.getIdUsuario();
        }
        throw new ChatAccesoDenegadoException("La sesión no está autenticada.");
    }
}
