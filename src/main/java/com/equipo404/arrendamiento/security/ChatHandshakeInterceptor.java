package com.equipo404.arrendamiento.security;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.WebUtils;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    static final String SESSION_KEY = "chatHttpSession";
    static final String CSRF_COOKIE_KEY = "chatCsrfCookie";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler handler,
            Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            return false;
        }
        var csrfCookie = WebUtils.getCookie(httpRequest, "XSRF-TOKEN");
        attributes.put(SESSION_KEY, session);
        attributes.put(CSRF_COOKIE_KEY, csrfCookie == null ? null : csrfCookie.getValue());
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler handler,
            Exception exception) {
        // No hay estado adicional que limpiar.
    }
}
