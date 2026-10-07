package com.equipo404.arrendamiento.security;

import com.equipo404.arrendamiento.exception.ChatAccesoDenegadoException;
import com.equipo404.arrendamiento.service.ConversacionService;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ConversacionChannelInterceptor implements ChannelInterceptor {

    private static final Pattern DESTINO_CONVERSACION =
            Pattern.compile("^/topic/conversaciones/([1-9][0-9]*)$");
    private static final Pattern DESTINO_ENVIO =
            Pattern.compile("^/app/conversaciones/([1-9][0-9]*)/mensajes$");

    private final ConversacionService conversacionService;

    public ConversacionChannelInterceptor(ConversacionService conversacionService) {
        this.conversacionService = conversacionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT -> {
                validarSesion(accessor);
                validarTokenCsrf(accessor);
            }
            case SUBSCRIBE -> validarSuscripcion(accessor);
            case SEND -> validarEnvio(accessor);
            case DISCONNECT, UNSUBSCRIBE -> validarSesion(accessor);
            default -> throw new ChatAccesoDenegadoException("Operación de chat no permitida.");
        }
        return message;
    }

    private void validarTokenCsrf(StompHeaderAccessor accessor) {
        Map<String, Object> atributos = accessor.getSessionAttributes();
        String csrfCookie = atributos == null
                ? null : (String) atributos.get(ChatHandshakeInterceptor.CSRF_COOKIE_KEY);
        String csrfHeader = accessor.getFirstNativeHeader("X-XSRF-TOKEN");
        if (csrfCookie == null || csrfHeader == null
                || !MessageDigest.isEqual(
                        csrfCookie.getBytes(StandardCharsets.UTF_8),
                        csrfHeader.getBytes(StandardCharsets.UTF_8))) {
            throw new ChatAccesoDenegadoException("Actualiza el token CSRF antes de conectar al chat.");
        }
    }

    private void validarSuscripcion(StompHeaderAccessor accessor) {
        Long idUsuario = validarSesion(accessor);
        String destino = accessor.getDestination();
        if ("/user/queue/errores".equals(destino)) {
            return;
        }
        Matcher matcher = destino == null ? null : DESTINO_CONVERSACION.matcher(destino);
        if (matcher == null || !matcher.matches()) {
            throw new ChatAccesoDenegadoException("Solo puedes abrir canales de tus conversaciones.");
        }
        conversacionService.validarParticipante(Long.valueOf(matcher.group(1)), idUsuario);
    }

    private void validarEnvio(StompHeaderAccessor accessor) {
        Long idUsuario = validarSesion(accessor);
        String destino = accessor.getDestination();
        Matcher matcher = destino == null ? null : DESTINO_ENVIO.matcher(destino);
        if (matcher == null || !matcher.matches()) {
            throw new ChatAccesoDenegadoException("Solo se permite enviar mensajes a conversaciones.");
        }
        conversacionService.validarParticipante(Long.valueOf(matcher.group(1)), idUsuario);
    }

    private Long validarSesion(StompHeaderAccessor accessor) {
        Long idUsuario = idUsuario(accessor);
        Map<String, Object> atributos = accessor.getSessionAttributes();
        Object valorSesion = atributos == null
                ? null : atributos.get(ChatHandshakeInterceptor.SESSION_KEY);
        if (!(valorSesion instanceof HttpSession session)) {
            throw new ChatAccesoDenegadoException("La sesión del chat ya no es válida.");
        }
        try {
            session.getLastAccessedTime();
        } catch (IllegalStateException ex) {
            throw new ChatAccesoDenegadoException("La sesión del chat ya no es válida.");
        }
        return idUsuario;
    }

    private Long idUsuario(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UsuarioPrincipal usuario) {
            return usuario.getIdUsuario();
        }
        throw new ChatAccesoDenegadoException("La sesión no está autenticada.");
    }
}
