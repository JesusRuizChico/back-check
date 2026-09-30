package com.equipo404.arrendamiento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConversacionIntegrationTests {

    private static final String CLAVE = "ClaveDePrueba123!";

    @Value("${local.server.port}")
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final Set<Long> usuariosCreados = new HashSet<>();
    private final Set<Long> propiedadesCreadas = new HashSet<>();
    private final Set<Long> conversacionesCreadas = new HashSet<>();

    @BeforeEach
    void limpiarResiduosDeEjecucionesAnteriores() {
        String usuariosChatPrueba = """
                select id_usuario from usuarios
                where correo like 'chat-%@example.com' and nombre = 'Usuario de chat'
                """;
        jdbc.update("""
                delete from mensajes m using conversaciones c
                where m.id_conversacion = c.id_conversacion
                  and (c.id_usuario_1 in (%s) or c.id_usuario_2 in (%s))
                """.formatted(usuariosChatPrueba, usuariosChatPrueba));
        jdbc.update("""
                delete from conversaciones c
                where c.id_usuario_1 in (%s) or c.id_usuario_2 in (%s)
                """.formatted(usuariosChatPrueba, usuariosChatPrueba));
        jdbc.update("delete from propiedades where id_arrendador in (" + usuariosChatPrueba + ")");
        jdbc.update("delete from usuario_rol where id_usuario in (" + usuariosChatPrueba + ")");
        jdbc.update("delete from usuarios where id_usuario in (" + usuariosChatPrueba + ")");
    }

    @AfterEach
    void limpiarDatosDePrueba() {
        for (Long idConversacion : conversacionesCreadas) {
            jdbc.update("delete from mensajes where id_conversacion = ?", idConversacion);
            jdbc.update("delete from conversaciones where id_conversacion = ?", idConversacion);
        }
        for (Long idPropiedad : propiedadesCreadas) {
            jdbc.update("delete from propiedades where id_propiedad = ?", idPropiedad);
        }
        for (Long idUsuario : usuariosCreados) {
            jdbc.update("delete from usuario_rol where id_usuario = ?", idUsuario);
            jdbc.update("delete from usuarios where id_usuario = ?", idUsuario);
        }
    }

    @Test
    void reutilizaConversacionValidaMensajesYRespetaParticipantes() throws Exception {
        long idArrendador = crearUsuario("arrendador");
        long idArrendatario = crearUsuario("arrendatario");
        long idAjeno = crearUsuario("arrendatario");
        jdbc.update("update usuarios set ultimo_acceso = ? where id_usuario = ?",
                OffsetDateTime.now().minusDays(6), idArrendador);
        long idPropiedad = crearPropiedad(idArrendador, "Casa uno");
        long idOtraPropiedad = crearPropiedad(idArrendador, "Casa dos");

        Cliente arrendatario = iniciarSesion(idArrendatario);
        Cliente arrendador = new Cliente();
        Cliente ajeno = iniciarSesion(idAjeno);

        var consultaInicial = arrendatario.enviar("GET",
                "/api/conversaciones/propiedad/" + idPropiedad, null, false);
        assertEquals(200, consultaInicial.statusCode(), consultaInicial.body());
        assertTrue(mapper.readTree(consultaInicial.body()).get("idConversacion").isNull());
        assertEquals(idArrendador, mapper.readTree(consultaInicial.body())
                .get("arrendador").get("idUsuario").asLong());

        var primera = arrendatario.enviar("POST",
                "/api/conversaciones/propiedad/" + idPropiedad + "/mensajes",
                Map.of("contenido", "¿Sigue disponible?"), true);
        assertEquals(201, primera.statusCode(), primera.body());
        JsonNode primeraRespuesta = mapper.readTree(primera.body());
        long idConversacion = primeraRespuesta.get("idConversacion").asLong();
        conversacionesCreadas.add(idConversacion);
        assertTrue(primeraRespuesta.get("avisoRespuestaLenta").asBoolean());

        var consultaPosterior = arrendatario.enviar("GET",
                "/api/conversaciones/propiedad/" + idOtraPropiedad, null, false);
        assertEquals(200, consultaPosterior.statusCode(), consultaPosterior.body());
        assertEquals(idConversacion,
                mapper.readTree(consultaPosterior.body()).get("idConversacion").asLong());

        var segunda = arrendatario.enviar("POST",
                "/api/conversaciones/propiedad/" + idOtraPropiedad + "/mensajes",
                Map.of("contenido", "También me interesa la otra casa."), true);
        assertEquals(200, segunda.statusCode(), segunda.body());
        assertEquals(idConversacion,
                mapper.readTree(segunda.body()).get("idConversacion").asLong());

        arrendador.iniciarSesion(correoDe(idArrendador));
        CompletableFuture<Map> eventoRecibido = new CompletableFuture<>();
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompSession sesionArrendatario = conectar(arrendatario, stompClient);
        sesionArrendatario.subscribe("/topic/conversaciones/" + idConversacion,
                new StompFrameHandler() {
                    @Override
                    public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                        return Map.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        eventoRecibido.complete((Map) payload);
                    }
                });
        StompSession sesionArrendador = conectar(arrendador, stompClient);
        StompHeaders encabezadosMensaje = new StompHeaders();
        encabezadosMensaje.setDestination("/app/conversaciones/" + idConversacion + "/mensajes");
        encabezadosMensaje.setContentType(MediaType.APPLICATION_JSON);
        sesionArrendador.send(encabezadosMensaje, Map.of("contenido", "Te respondo por el chat."));
        Map evento = eventoRecibido.get(10, TimeUnit.SECONDS);
        assertEquals("MENSAJE", evento.get("tipo"));
        sesionArrendatario.disconnect();
        sesionArrendador.disconnect();
        stompClient.stop();

        var vacio = arrendatario.enviar("POST",
                "/api/conversaciones/" + idConversacion + "/mensajes",
                Map.of("contenido", "   "), true);
        assertEquals(400, vacio.statusCode(), vacio.body());

        var historial = arrendador.enviar("GET",
                "/api/conversaciones/" + idConversacion + "/mensajes", null, false);
        assertEquals(200, historial.statusCode(), historial.body());
        assertEquals(3, mapper.readTree(historial.body()).get("mensajes").size());

        var marcarLectura = arrendador.enviar("PUT",
                "/api/conversaciones/" + idConversacion + "/lectura", null, true);
        assertEquals(200, marcarLectura.statusCode(), marcarLectura.body());
        assertEquals(2, mapper.readTree(marcarLectura.body()).get("mensajesMarcados").asInt());

        var accesoAjeno = ajeno.enviar("GET",
                "/api/conversaciones/" + idConversacion + "/mensajes", null, false);
        assertEquals(404, accesoAjeno.statusCode(), accesoAjeno.body());

        var lista = arrendatario.enviar("GET", "/api/conversaciones", null, false);
        assertEquals(200, lista.statusCode(), lista.body());
        assertEquals(1, mapper.readTree(lista.body()).size());
        assertEquals(1, mapper.readTree(lista.body()).get(0).get("mensajesNoLeidos").asInt());
    }

    @Test
    void catalogoMuestraPropiedadesDisponiblesYNoIncluyeLasPausadas() throws Exception {
        long idArrendador = crearUsuario("arrendador");
        long idArrendatario = crearUsuario("arrendatario");
        long idDisponible = crearPropiedad(idArrendador, "Casa disponible");
        long idPausada = crearPropiedad(idArrendador, "Casa pausada");
        jdbc.update("update propiedades set estado = 'pausada' where id_propiedad = ?", idPausada);

        Cliente arrendatario = iniciarSesion(idArrendatario);
        var respuesta = arrendatario.enviar("GET", "/api/propiedades", null, false);

        assertEquals(200, respuesta.statusCode(), respuesta.body());
        var catalogo = mapper.readTree(respuesta.body());
        boolean incluyeDisponible = false;
        boolean incluyePausada = false;
        for (JsonNode propiedad : catalogo) {
            long id = propiedad.get("idPropiedad").asLong();
            incluyeDisponible |= id == idDisponible;
            incluyePausada |= id == idPausada;
        }
        assertTrue(incluyeDisponible, "El catálogo debe incluir la propiedad disponible");
        assertFalse(incluyePausada, "El catálogo no debe incluir propiedades pausadas");
    }

    private long crearUsuario(String rol) {
        String correo = "chat-test-" + UUID.randomUUID() + "@example.com";
        Long idUsuario = jdbc.queryForObject(
                "insert into usuarios (password, nombre, correo) values (?, ?, ?) returning id_usuario",
                Long.class, passwordEncoder.encode(CLAVE), "Usuario de chat", correo);
        usuariosCreados.add(idUsuario);
        jdbc.update("""
                insert into usuario_rol (id_usuario, id_rol)
                select ?, id_rol from roles where nombre = ?
                """, idUsuario, rol);
        return idUsuario;
    }

    private String correoDe(long idUsuario) {
        return jdbc.queryForObject("select correo from usuarios where id_usuario = ?", String.class, idUsuario);
    }

    private long crearPropiedad(long idArrendador, String titulo) {
        Long idPropiedad = jdbc.queryForObject("""
                insert into propiedades (
                    id_arrendador, titulo, descripcion, precio_mensual, calle,
                    numero_exterior, colonia, municipio, estado_ubicacion, codigo_postal
                ) values (?, ?, 'Descripción de prueba', 5000, 'Calle de prueba',
                    '10', 'Centro', 'Querétaro', 'Querétaro', '76000')
                returning id_propiedad
                """, Long.class, idArrendador, titulo);
        propiedadesCreadas.add(idPropiedad);
        return idPropiedad;
    }

    private Cliente iniciarSesion(long idUsuario) throws Exception {
        Cliente cliente = new Cliente();
        cliente.iniciarSesion(correoDe(idUsuario));
        return cliente;
    }

    private StompSession conectar(Cliente cliente, WebSocketStompClient stompClient) throws Exception {
        WebSocketHttpHeaders encabezadosHttp = new WebSocketHttpHeaders();
        encabezadosHttp.add("Origin", "http://localhost:" + port);
        encabezadosHttp.add("Cookie", cliente.cookies.getCookieStore().getCookies().stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(java.util.stream.Collectors.joining("; ")));
        StompHeaders encabezadosStomp = new StompHeaders();
        encabezadosStomp.add("X-XSRF-TOKEN", cliente.token);
        return stompClient.connectAsync(
                URI.create("ws://localhost:" + port + "/ws"),
                encabezadosHttp,
                encabezadosStomp,
                new StompSessionHandlerAdapter() {
                }).get(10, TimeUnit.SECONDS);
    }

    private class Cliente {
        final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        final HttpClient http = HttpClient.newBuilder().cookieHandler(cookies).build();
        String token;

        HttpResponse<String> enviar(String metodo, String ruta, Object cuerpo, boolean csrf)
                throws Exception {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + ruta))
                    .header("Content-Type", "application/json");
            if (csrf && token != null) {
                request.header("X-XSRF-TOKEN", token);
            }
            request.method(metodo, cuerpo == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)));
            return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }

        void renovarCsrf() throws Exception {
            var respuesta = enviar("GET", "/api/csrf", null, false);
            assertEquals(200, respuesta.statusCode(), respuesta.body());
            token = mapper.readTree(respuesta.body()).get("token").asText();
        }

        void iniciarSesion(String correo) throws Exception {
            renovarCsrf();
            var respuesta = enviar("POST", "/api/auth/login",
                    Map.of("correo", correo, "contrasena", CLAVE), true);
            assertEquals(200, respuesta.statusCode(), respuesta.body());
            renovarCsrf();
        }
    }
}
