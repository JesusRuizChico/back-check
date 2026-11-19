package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.EnviarMensajeRequest;
import com.equipo404.arrendamiento.dto.response.ConversacionPropiedadResponse;
import com.equipo404.arrendamiento.dto.response.InicioConversacionResponse;
import com.equipo404.arrendamiento.dto.response.LecturaResponse;
import com.equipo404.arrendamiento.dto.response.MensajeEnviadoResponse;
import com.equipo404.arrendamiento.dto.response.MensajePaginaResponse;
import com.equipo404.arrendamiento.dto.response.ResumenConversacionResponse;
import java.util.List;

public interface ConversacionService {

    InicioConversacionResponse iniciarDesdePropiedad(
            Long idUsuarioArrendatario, Long idPropiedad, EnviarMensajeRequest request);

    ConversacionPropiedadResponse resolverDesdePropiedad(Long idUsuarioArrendatario, Long idPropiedad);

    List<ResumenConversacionResponse> listar(Long idUsuario);

    MensajePaginaResponse listarMensajes(Long idConversacion, Long idUsuario, Long antesDeIdMensaje, int limite);

    MensajeEnviadoResponse enviar(Long idConversacion, Long idUsuarioEmisor, EnviarMensajeRequest request);

    LecturaResponse marcarComoLeidos(Long idConversacion, Long idUsuario);
}
