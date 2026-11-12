package com.equipo404.arrendamiento;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.equipo404.arrendamiento.support.ClienteApi;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ControlAccesoIntegrationTests {

    private static final String CLAVE = "ClaveDePrueba123!";

    @Value("${local.server.port}")
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void limpiar() {
        String usuarios = "select id_usuario from usuarios where correo like 'acceso-test-%@example.com'";
        jdbc.update("delete from usuario_rol where id_usuario in (" + usuarios + ")");
        jdbc.update("delete from usuarios where id_usuario in (" + usuarios + ")");
    }

    @Test
    void bloqueaLaCuentaTrasCincoIntentosFallidosYLaLiberaAlExpirar() throws Exception {
        String correo = "acceso-test-" + UUID.randomUUID() + "@example.com";
        ClienteApi cliente = new ClienteApi(port, mapper);
        cliente.renovarCsrf();
        assertEquals(201, cliente.json("POST", "/api/auth/registro", Map.of(
                "nombre", "Ana Prueba", "correo", correo, "contrasena", CLAVE,
                "rolSolicitado", "arrendatario")).statusCode());

        Map<String, String> incorrecto = Map.of("correo", correo, "contrasena", "incorrecta");
        for (int intento = 1; intento < 5; intento++) {
            assertEquals(401, cliente.json("POST", "/api/auth/login", incorrecto).statusCode());
        }
        assertEquals(423, cliente.json("POST", "/api/auth/login", incorrecto).statusCode());
        assertEquals(423, cliente.json("POST", "/api/auth/login",
                Map.of("correo", correo, "contrasena", CLAVE)).statusCode());

        jdbc.update("update usuarios set bloqueado_hasta = now() - interval '1 minute' where correo = ?", correo);
        assertEquals(200, cliente.json("POST", "/api/auth/login",
                Map.of("correo", correo, "contrasena", CLAVE)).statusCode());
        assertEquals(0, jdbc.queryForObject(
                "select intentos_fallidos from usuarios where correo = ?", Integer.class, correo));
    }
}
