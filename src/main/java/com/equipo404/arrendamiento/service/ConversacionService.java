package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.EnviarMensajeRequest;
import com.equipo404.arrendamiento.dto.response.InicioConversacionResponse;
import com.equipo404.arrendamiento.dto.response.LecturaResponse;
import com.equipo404.arrendamiento.dto.response.MensajeEnviadoResponse;
import com.equipo404.arrendamiento.dto.response.MensajePaginaResponse;
import com.equipo404.arrendamiento.dto.response.MensajeResponse;
import com.equipo404.arrendamiento.dto.response.ResumenConversacionResponse;
import com.equipo404.arrendamiento.dto.response.UsuarioChatResponse;
import com.equipo404.arrendamiento.entity.Conversacion;
import com.equipo404.arrendamiento.entity.Mensaje;
import com.equipo404.arrendamiento.entity.Propiedad;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.exception.ChatAccesoDenegadoException;
import com.equipo404.arrendamiento.exception.RecursoNoEncontradoException;
import com.equipo404.arrendamiento.repository.ConversacionRepository;
import com.equipo404.arrendamiento.repository.MensajeRepository;
import com.equipo404.arrendamiento.repository.PropiedadRepository;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRolRepository;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversacionService {

    private static final int MAX_CARACTERES_MENSAJE = 4000;
    private static final int MAX_TAMANO_PAGINA = 100;
    private static final int DIAS_INACTIVIDAD = 5;

    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final EntityManager entityManager;

    public ConversacionService(
            ConversacionRepository conversacionRepository,
            MensajeRepository mensajeRepository,
            PropiedadRepository propiedadRepository,
            UsuarioRepository usuarioRepository,
            UsuarioRolRepository usuarioRolRepository,
            EntityManager entityManager) {
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public InicioConversacionResponse iniciarDesdePropiedad(
            Long idUsuarioArrendatario,
            Long idPropiedad,
            EnviarMensajeRequest request) {
        Usuario arrendatario = obtenerUsuario(idUsuarioArrendatario);
        if (!usuarioRolRepository.tieneRol(idUsuarioArrendatario, "arrendatario")) {
            throw new ChatAccesoDenegadoException("Solo un arrendatario puede iniciar este contacto.");
        }

        Propiedad propiedad = propiedadRepository.findById(idPropiedad)
                .orElseThrow(() -> new RecursoNoEncontradoException("La propiedad no existe."));
        if ("eliminada".equals(propiedad.getEstado())) {
            throw new RecursoNoEncontradoException("La propiedad no está disponible para contacto.");
        }

        Usuario arrendador = propiedad.getArrendador();
        if (arrendador.getIdUsuario().equals(idUsuarioArrendatario)) {
            throw new IllegalArgumentException("No puedes iniciar una conversación contigo mismo.");
        }
        if (!"activo".equals(arrendador.getEstado())
                || !usuarioRolRepository.tieneRol(arrendador.getIdUsuario(), "arrendador")) {
            throw new RecursoNoEncontradoException("El arrendador no está disponible.");
        }

        Long idUsuario1 = Math.min(idUsuarioArrendatario, arrendador.getIdUsuario());
        Long idUsuario2 = Math.max(idUsuarioArrendatario, arrendador.getIdUsuario());
        boolean conversacionNueva = conversacionRepository.crearSiNoExiste(idUsuario1, idUsuario2) > 0;
        Conversacion conversacion = conversacionRepository
                .findByUsuario1_IdUsuarioAndUsuario2_IdUsuario(idUsuario1, idUsuario2)
                .orElseThrow(() -> new IllegalStateException("No se pudo abrir la conversación."));

        Mensaje mensaje = crearMensaje(conversacion, arrendatario, request.contenido());
        boolean aviso = arrendadorInactivo(arrendador);
        return new InicioConversacionResponse(
                conversacion.getIdConversacion(),
                conversacionNueva,
                mapearUsuario(arrendador),
                mapearMensaje(mensaje),
                aviso,
                arrendador.getUltimoAcceso());
    }

    @Transactional(readOnly = true)
    public List<ResumenConversacionResponse> listar(Long idUsuario) {
        List<Conversacion> conversaciones = conversacionRepository.listarDelUsuario(idUsuario);
        List<ResumenConversacionResponse> respuesta = new ArrayList<>(conversaciones.size());
        for (Conversacion conversacion : conversaciones) {
            Usuario otro = obtenerOtroParticipante(conversacion, idUsuario);
            Mensaje ultimo = mensajeRepository.findTopByConversacion_IdConversacionOrderByIdMensajeDesc(
                    conversacion.getIdConversacion());
            long noLeidos = mensajeRepository
                    .countByConversacion_IdConversacionAndEmisor_IdUsuarioNotAndFechaLecturaIsNull(
                            conversacion.getIdConversacion(), idUsuario);
            Mensaje primero = mensajeRepository.findTopByConversacion_IdConversacionOrderByIdMensajeAsc(
                    conversacion.getIdConversacion());
            boolean arrendatarioActual = primero != null
                    && primero.getEmisor().getIdUsuario().equals(idUsuario);
            respuesta.add(new ResumenConversacionResponse(
                    conversacion.getIdConversacion(),
                    mapearUsuario(otro),
                    ultimo == null ? null : mapearMensaje(ultimo),
                    conversacion.getFechaCreacion(),
                    conversacion.getFechaUltimoMensaje(),
                    noLeidos,
                    arrendatarioActual && arrendadorInactivo(otro)));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public MensajePaginaResponse listarMensajes(
            Long idConversacion,
            Long idUsuario,
            Long antesDeIdMensaje,
            int limite) {
        validarLimite(limite);
        Conversacion conversacion = obtenerConversacionParticipante(idConversacion, idUsuario);
        List<Mensaje> descendentes = antesDeIdMensaje == null
                ? mensajeRepository.findByConversacion_IdConversacionOrderByIdMensajeDesc(
                        conversacion.getIdConversacion(), PageRequest.of(0, limite + 1))
                : mensajeRepository.findByConversacion_IdConversacionAndIdMensajeLessThanOrderByIdMensajeDesc(
                        conversacion.getIdConversacion(), antesDeIdMensaje, PageRequest.of(0, limite + 1));

        boolean hayMas = descendentes.size() > limite;
        if (hayMas) {
            descendentes = new ArrayList<>(descendentes.subList(0, limite));
        }
        Collections.reverse(descendentes);
        List<MensajeResponse> mensajes = descendentes.stream().map(this::mapearMensaje).toList();
        Long siguienteAntesDe = hayMas && !descendentes.isEmpty()
                ? descendentes.get(0).getIdMensaje()
                : null;
        return new MensajePaginaResponse(mensajes, siguienteAntesDe, hayMas);
    }

    @Transactional
    public MensajeEnviadoResponse enviar(
            Long idConversacion,
            Long idUsuarioEmisor,
            EnviarMensajeRequest request) {
        Conversacion conversacion = obtenerConversacionParticipante(idConversacion, idUsuarioEmisor);
        Usuario emisor = obtenerUsuario(idUsuarioEmisor);
        Usuario receptor = obtenerOtroParticipante(conversacion, idUsuarioEmisor);
        Mensaje mensaje = crearMensaje(conversacion, emisor, request.contenido());
        Mensaje primero = mensajeRepository.findTopByConversacion_IdConversacionOrderByIdMensajeAsc(
                idConversacion);
        boolean aviso = primero != null
                && primero.getEmisor().getIdUsuario().equals(idUsuarioEmisor)
                && arrendadorInactivo(receptor);
        return new MensajeEnviadoResponse(
                mapearMensaje(mensaje), aviso, aviso ? receptor.getUltimoAcceso() : null);
    }

    @Transactional
    public LecturaResponse marcarComoLeidos(Long idConversacion, Long idUsuario) {
        obtenerConversacionParticipante(idConversacion, idUsuario);
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        int marcados = mensajeRepository.marcarComoLeidos(idConversacion, idUsuario, ahora);
        return new LecturaResponse(marcados, ahora);
    }

    @Transactional(readOnly = true)
    public void validarParticipante(Long idConversacion, Long idUsuario) {
        obtenerConversacionParticipante(idConversacion, idUsuario);
    }

    private Mensaje crearMensaje(Conversacion conversacion, Usuario emisor, String contenido) {
        String texto = contenido == null ? "" : contenido.strip();
        if (texto.isBlank()) {
            throw new IllegalArgumentException("Escribe un mensaje antes de enviarlo.");
        }
        if (texto.length() > MAX_CARACTERES_MENSAJE) {
            throw new IllegalArgumentException("El mensaje no puede superar los 4000 caracteres.");
        }
        Mensaje mensaje = mensajeRepository.save(new Mensaje(conversacion, emisor, texto));
        mensajeRepository.flush();
        entityManager.refresh(mensaje);
        entityManager.refresh(conversacion);
        return mensaje;
    }

    private Conversacion obtenerConversacionParticipante(Long idConversacion, Long idUsuario) {
        Conversacion conversacion = conversacionRepository.findById(idConversacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("La conversación no existe."));
        if (!conversacion.getUsuario1().getIdUsuario().equals(idUsuario)
                && !conversacion.getUsuario2().getIdUsuario().equals(idUsuario)) {
            throw new RecursoNoEncontradoException("La conversación no existe.");
        }
        return conversacion;
    }

    private Usuario obtenerOtroParticipante(Conversacion conversacion, Long idUsuario) {
        if (conversacion.getUsuario1().getIdUsuario().equals(idUsuario)) {
            return conversacion.getUsuario2();
        }
        if (conversacion.getUsuario2().getIdUsuario().equals(idUsuario)) {
            return conversacion.getUsuario1();
        }
        throw new RecursoNoEncontradoException("La conversación no existe.");
    }

    private Usuario obtenerUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));
    }

    private boolean arrendadorInactivo(Usuario arrendador) {
        OffsetDateTime limite = OffsetDateTime.now(ZoneOffset.UTC).minusDays(DIAS_INACTIVIDAD);
        return arrendador.getUltimoAcceso() == null || !arrendador.getUltimoAcceso().isAfter(limite);
    }

    private void validarLimite(int limite) {
        if (limite < 1 || limite > MAX_TAMANO_PAGINA) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 100 mensajes.");
        }
    }

    private UsuarioChatResponse mapearUsuario(Usuario usuario) {
        return new UsuarioChatResponse(usuario.getIdUsuario(), usuario.getNombre(), usuario.getFotoPerfil());
    }

    private MensajeResponse mapearMensaje(Mensaje mensaje) {
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
