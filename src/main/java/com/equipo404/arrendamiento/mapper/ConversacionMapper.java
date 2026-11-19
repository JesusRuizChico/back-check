package com.equipo404.arrendamiento.mapper;

import com.equipo404.arrendamiento.dto.response.MensajeResponse;
import com.equipo404.arrendamiento.dto.response.UsuarioChatResponse;
import com.equipo404.arrendamiento.entity.Mensaje;
import com.equipo404.arrendamiento.entity.Usuario;

public final class ConversacionMapper {

    private ConversacionMapper() {
    }

    public static UsuarioChatResponse toUsuarioChat(Usuario usuario) {
        return new UsuarioChatResponse(usuario.getIdUsuario(), usuario.getNombre(), usuario.getFotoPerfil());
    }

    public static MensajeResponse toMensaje(Mensaje mensaje) {
        Usuario emisor = mensaje.getEmisor();
        return new MensajeResponse(
                mensaje.getIdMensaje(),
                emisor.getIdUsuario(),
                emisor.getNombre(),
                mensaje.getContenido(),
                mensaje.getFechaEnvio(),
                mensaje.getFechaLectura());
    }
}
