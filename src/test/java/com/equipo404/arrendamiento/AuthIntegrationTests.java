package com.equipo404.arrendamiento;

import com.equipo404.arrendamiento.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTests {
    @Value("${local.server.port}") int port;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbc;

    private class Cliente {
        final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        final HttpClient http = HttpClient.newBuilder().cookieHandler(cookies).build();
        String token;

        HttpResponse<String> enviar(String metodo, String ruta, Object cuerpo, boolean csrf)
                throws Exception {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + ruta))
                    .header("Content-Type", "application/json");
            if (csrf && token != null) request.header("X-XSRF-TOKEN", token);
            request.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)));
            return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }

        void renovarCsrf() throws Exception {
            var respuesta = enviar("GET", "/api/csrf", null, false);
            assertEquals(200, respuesta.statusCode(), respuesta.body());
            token = mapper.readTree(respuesta.body()).get("token").asText();
        }

        String sesion() {
            return cookies.getCookieStore().getCookies().stream()
                    .filter(c -> c.getName().equals("JSESSIONID"))
                    .findFirst().orElseThrow().getValue();
        }
    }

    private String correo() { return UUID.randomUUID() + "@example.com"; }

    private Map<String, String> registro(String correo, String rol) {
        return Map.of("nombre", "Ana Prueba", "correo", correo,
                "contrasena", "ClaveDePrueba123!", "rolSolicitado", rol);
    }

    private Map<String, String> login(String correo, String clave) {
        return Map.of("correo", correo, "contrasena", clave);
    }

    @Test
    void registroLoginPerfilRotacionYLogout() throws Exception {
        var cliente = new Cliente();
        String correo = correo();
        assertEquals(401, cliente.enviar("GET", "/api/perfil", null, false).statusCode());
        cliente.renovarCsrf();
        var alta = cliente.enviar("POST", "/api/auth/registro", registro(correo, "arrendatario"), true);
        assertEquals(201, alta.statusCode(), alta.body());
        assertFalse(alta.body().contains("passwordHash"));
        assertEquals(401, cliente.enviar("GET", "/api/perfil", null, false).statusCode());
        var usuario = usuarios.findByCorreo(correo).orElseThrow();
        assertTrue(passwordEncoder.matches("ClaveDePrueba123!", usuario.getPasswordHash()));
        String csrfAnterior = cliente.token;
        var acceso = cliente.enviar("POST", "/api/auth/login", login(correo, "ClaveDePrueba123!"), true);
        assertEquals(200, acceso.statusCode(), acceso.body());
        assertNotNull(usuarios.findByCorreo(correo).orElseThrow().getUltimoAcceso());
        var perfil = cliente.enviar("GET", "/api/perfil", null, false);
        assertEquals(200, perfil.statusCode(), perfil.body());
        JsonNode datos = mapper.readTree(perfil.body());
        assertEquals(correo, datos.get("correo").asText());
        assertEquals("arrendatario", datos.get("rolesActivos").get(0).asText());
        assertFalse(perfil.body().contains("passwordHash"));
        String sesionAnterior = cliente.sesion();
        cliente.renovarCsrf();
        assertNotEquals(csrfAnterior, cliente.token);
        assertEquals(200, cliente.enviar("POST", "/api/auth/login",
                login(correo, "ClaveDePrueba123!"), true).statusCode());
        assertNotEquals(sesionAnterior, cliente.sesion());
        cliente.renovarCsrf();
        assertEquals(403, cliente.enviar("POST", "/api/auth/logout", null, false).statusCode());
        String sesionCerrada = cliente.sesion();
        assertEquals(204, cliente.enviar("POST", "/api/auth/logout", null, true).statusCode());
        assertEquals(401, cliente.enviar("GET", "/api/perfil", null, false).statusCode());
        var replay = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/perfil"))
                .header("Cookie", "JSESSIONID=" + sesionCerrada).GET().build();
        assertEquals(401, HttpClient.newHttpClient().send(replay,
                HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void validacionesDuplicadosRolesYCredenciales() throws Exception {
        var cliente = new Cliente();
        String correo = correo();
        assertEquals(403, cliente.enviar("POST", "/api/auth/registro", registro(correo, "arrendador"), false).statusCode());
        cliente.renovarCsrf();
        assertEquals(400, cliente.enviar("POST", "/api/auth/registro", Map.of(), true).statusCode());
        assertEquals(400, cliente.enviar("POST", "/api/auth/registro", registro("correo-invalido", "arrendador"), true).statusCode());
        assertEquals(400, cliente.enviar("POST", "/api/auth/registro", registro(correo, "administrador"), true).statusCode());
        assertFalse(usuarios.existsByCorreo(correo));
        assertEquals(201, cliente.enviar("POST", "/api/auth/registro", registro(correo, "arrendador"), true).statusCode());
        assertEquals(409, cliente.enviar("POST", "/api/auth/registro", registro(correo.toUpperCase(), "arrendador"), true).statusCode());
        assertEquals(401, cliente.enviar("POST", "/api/auth/login", login(correo, "incorrecta"), true).statusCode());
        assertEquals(401, cliente.enviar("POST", "/api/auth/login", login(correo(), "incorrecta"), true).statusCode());
        assertEquals(401, cliente.enviar("GET", "/api/perfil", null, false).statusCode());
        jdbc.update("update usuarios set estado='revocado' where correo=?", correo);
        assertEquals(401, cliente.enviar("POST", "/api/auth/login", login(correo, "ClaveDePrueba123!"), true).statusCode());
    }
}
