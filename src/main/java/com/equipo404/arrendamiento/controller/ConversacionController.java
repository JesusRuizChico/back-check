package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.request.EnviarMensajeRequest;
import com.equipo404.arrendamiento.dto.response.EventoConversacionResponse;
import com.equipo404.arrendamiento.dto.response.InicioConversacionResponse;
import com.equipo404.arrendamiento.dto.response.LecturaResponse;
import com.equipo404.arrendamiento.dto.response.MensajeEnviadoResponse;
import com.equipo404.arrendamiento.dto.response.MensajePaginaResponse;
import com.equipo404.arrendamiento.dto.response.ResumenConversacionResponse;
import com.equipo404.arrendamiento.security.UsuarioPrincipal;
import com.equipo404.arrendamiento.service.ConversacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversaciones")
public class ConversacionController {

    private final ConversacionService conversacionService;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversacionController(
            ConversacionService conversacionService,
            SimpMessagingTemplate messagingTemplate) {
        this.conversacionService = conversacionService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/propiedad/{idPropiedad}/mensajes")
    public ResponseEntity<InicioConversacionResponse> iniciarDesdePropiedad(
            @PathVariable Long idPropiedad,
            @Valid @RequestBody EnviarMensajeRequest request,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        InicioConversacionResponse respuesta = conversacionService.iniciarDesdePropiedad(
                usuario.getIdUsuario(), idPropiedad, request);
        publicarMensaje(respuesta.idConversacion(), respuesta.mensajeInicial(),
                usuario.getIdUsuario(), respuesta.avisoRespuestaLenta());
        return ResponseEntity.status(respuesta.conversacionNueva()
                ? HttpStatus.CREATED : HttpStatus.OK).body(respuesta);
    }

    @GetMapping
    public List<ResumenConversacionResponse> listar(
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return conversacionService.listar(usuario.getIdUsuario());
    }

    @GetMapping("/{idConversacion}/mensajes")
    public MensajePaginaResponse listarMensajes(
            @PathVariable Long idConversacion,
            @RequestParam(required = false) Long antesDe,
            @RequestParam(defaultValue = "50") int limite,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return conversacionService.listarMensajes(
                idConversacion, usuario.getIdUsuario(), antesDe, limite);
    }

    @PostMapping("/{idConversacion}/mensajes")
    public MensajeEnviadoResponse enviar(
            @PathVariable Long idConversacion,
            @Valid @RequestBody EnviarMensajeRequest request,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        MensajeEnviadoResponse respuesta = conversacionService.enviar(
                idConversacion, usuario.getIdUsuario(), request);
        publicarMensaje(idConversacion, respuesta.mensaje(), usuario.getIdUsuario(),
                respuesta.avisoRespuestaLenta());
        return respuesta;
    }

    @PutMapping("/{idConversacion}/lectura")
    public LecturaResponse marcarComoLeidos(
            @PathVariable Long idConversacion,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        LecturaResponse respuesta = conversacionService.marcarComoLeidos(
                idConversacion, usuario.getIdUsuario());
        if (respuesta.mensajesMarcados() > 0) {
            messagingTemplate.convertAndSend(
                    destino(idConversacion),
                    new EventoConversacionResponse(
                            "LECTURA", null, usuario.getIdUsuario(), respuesta.fechaLectura(), false));
        }
        return respuesta;
    }

    void publicarMensaje(Long idConversacion, com.equipo404.arrendamiento.dto.response.MensajeResponse mensaje,
                         Long idEmisor, boolean avisoRespuestaLenta) {
        messagingTemplate.convertAndSend(
                destino(idConversacion),
                new EventoConversacionResponse(
                        "MENSAJE", mensaje, idEmisor, mensaje.fechaEnvio(), avisoRespuestaLenta));
    }

    private String destino(Long idConversacion) {
        return "/topic/conversaciones/" + idConversacion;
    }
}
