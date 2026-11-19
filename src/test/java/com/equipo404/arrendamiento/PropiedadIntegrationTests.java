package com.equipo404.arrendamiento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.equipo404.arrendamiento.support.ClienteApi;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PropiedadIntegrationTests {

    private static final String CLAVE = "ClaveDePrueba123!";
    private static final String PATRON_CORREOS = "prop-test-%@example.com";

    @Value("${local.server.port}")
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void limpiar() {
        String usuarios = "select id_usuario from usuarios where correo like '" + PATRON_CORREOS + "'";
        jdbc.update("delete from favoritos where id_usuario in (" + usuarios + ")");
        jdbc.update("delete from propiedades where id_arrendador in (" + usuarios + ")");
        jdbc.update("delete from usuario_rol where id_usuario in (" + usuarios + ")");
        jdbc.update("delete from usuarios where id_usuario in (" + usuarios + ")");
    }

    @Test
    void publicaConImagenLocalYGestionaElCicloDeVida() throws Exception {
        ClienteApi arrendador = registrarEIniciar("arrendador");
        ClienteApi inquilino = registrarEIniciar("arrendatario");
        ClienteApi otroArrendador = registrarEIniciar("arrendador");

        JsonNode servicios = mapper.readTree(arrendador.get("/api/servicios").body());
        assertTrue(servicios.size() >= 12, "Debe existir el catálogo de servicios incluidos");
        long idWifi = idServicio(servicios, "Internet / Wi-Fi");
        long idMascotas = idServicio(servicios, "Pet Friendly (Mascotas)");

        Map<String, String> campos = camposPropiedad();
        campos.put("serviciosIds[0]", String.valueOf(idWifi));
        campos.put("serviciosIds[1]", String.valueOf(idMascotas));

        assertEquals(400, arrendador.multipart("/api/propiedades", campos).statusCode(),
                "Una propiedad sin fotografías no se publica");
        assertEquals(403, inquilino.multipart("/api/propiedades", campos, png("foto")).statusCode(),
                "Un inquilino no puede publicar");

        var publicada = arrendador.multipart("/api/propiedades", campos, png("imagenes"));
        assertEquals(201, publicada.statusCode(), publicada.body());
        JsonNode propiedad = mapper.readTree(publicada.body());
        long idPropiedad = propiedad.get("idPropiedad").asLong();
        assertEquals(3, propiedad.get("habitaciones").asInt());
        assertEquals(2, propiedad.get("servicios").size());
        assertEquals("disponible", propiedad.get("estado").asText());
        String rutaImagen = propiedad.get("imagenes").get(0).asText();
        assertTrue(rutaImagen.startsWith("/uploads/propiedades/"), rutaImagen);

        var imagen = new ClienteApi(port, mapper).getBytes(rutaImagen);
        assertEquals(200, imagen.statusCode());
        assertEquals("image/png", imagen.headers().firstValue("Content-Type").orElse(""));

        JsonNode catalogo = mapper.readTree(inquilino.get("/api/propiedades").body());
        JsonNode enCatalogo = buscar(catalogo, idPropiedad);
        assertFalse(enCatalogo.get("esFavorito").asBoolean());
        assertEquals("Ana Prueba", enCatalogo.get("arrendador").get("nombre").asText());

        assertEquals(403, arrendador.json("PUT", "/api/favoritos/" + idPropiedad, null).statusCode());
        assertEquals(204, inquilino.json("PUT", "/api/favoritos/" + idPropiedad, null).statusCode());
        assertEquals(204, inquilino.json("PUT", "/api/favoritos/" + idPropiedad, null).statusCode());
        assertTrue(buscar(mapper.readTree(inquilino.get("/api/propiedades").body()), idPropiedad)
                .get("esFavorito").asBoolean());
        assertEquals(1, mapper.readTree(inquilino.get("/api/favoritos").body()).size());
        assertEquals(204, inquilino.json("DELETE", "/api/favoritos/" + idPropiedad, null).statusCode());
        assertEquals(0, mapper.readTree(inquilino.get("/api/favoritos").body()).size());

        assertEquals(404, otroArrendador.json("PATCH", "/api/propiedades/" + idPropiedad + "/estado",
                Map.of("estado", "pausada")).statusCode(), "Solo el dueño cambia el estado");
        var pausada = arrendador.json("PATCH", "/api/propiedades/" + idPropiedad + "/estado",
                Map.of("estado", "pausada"));
        assertEquals(200, pausada.statusCode(), pausada.body());
        assertEquals("pausada", mapper.readTree(pausada.body()).get("estado").asText());
        assertTrue(buscarOpcional(mapper.readTree(inquilino.get("/api/propiedades").body()), idPropiedad) == null,
                "Una propiedad pausada no aparece en el catálogo");
        assertEquals(1, mapper.readTree(arrendador.get("/api/propiedades/mis-propiedades").body()).size());

        assertEquals(204, arrendador.json("DELETE", "/api/propiedades/" + idPropiedad, null).statusCode());
        assertEquals(0, mapper.readTree(arrendador.get("/api/propiedades/mis-propiedades").body()).size());
        assertEquals(404, arrendador.get("/api/propiedades/" + idPropiedad).statusCode());
    }

    @Test
    void perfilEditableConContrasenaYFotoLocal() throws Exception {
        String correo = correo();
        ClienteApi cliente = registrar(correo, "arrendatario");
        cliente.iniciarSesion(correo, CLAVE);

        var actualizado = cliente.json("PUT", "/api/perfil",
                Map.of("nombre", "Ana Editada", "telefono", "4421234567", "biografia", "Busco depa"));
        assertEquals(200, actualizado.statusCode(), actualizado.body());
        JsonNode perfil = mapper.readTree(actualizado.body());
        assertEquals("Ana Editada", perfil.get("nombre").asText());
        assertEquals("Busco depa", perfil.get("biografia").asText());

        assertEquals(400, cliente.json("PUT", "/api/perfil/contrasena",
                Map.of("contrasenaActual", "incorrecta", "nuevaContrasena", "NuevaClave1")).statusCode());
        assertEquals(204, cliente.json("PUT", "/api/perfil/contrasena",
                Map.of("contrasenaActual", CLAVE, "nuevaContrasena", "NuevaClave1")).statusCode());
        new ClienteApi(port, mapper).iniciarSesion(correo, "NuevaClave1");

        var conFoto = cliente.multipart("/api/perfil/foto", Map.of(), png("foto"));
        assertEquals(200, conFoto.statusCode(), conFoto.body());
        String foto = mapper.readTree(conFoto.body()).get("fotoPerfil").asText();
        assertTrue(foto.startsWith("/uploads/perfiles/"), foto);
        assertEquals(200, cliente.getBytes(foto).statusCode());

        var sinFoto = cliente.json("DELETE", "/api/perfil/foto", null);
        assertEquals(200, sinFoto.statusCode());
        assertTrue(mapper.readTree(sinFoto.body()).get("fotoPerfil").isNull());
        assertEquals(404, cliente.getBytes(foto).statusCode(), "La foto anterior se elimina del disco");
    }

    @Test
    void registroPublicoSoloPermiteInquilinoYArrendador() throws Exception {
        ClienteApi cliente = new ClienteApi(port, mapper);
        cliente.renovarCsrf();
        assertEquals(400, cliente.json("POST", "/api/auth/registro", registro(correo(), "proveedor"))
                .statusCode());
        assertEquals(201, cliente.json("POST", "/api/auth/registro", registro(correo(), "arrendatario"))
                .statusCode());
        assertEquals(201, cliente.json("POST", "/api/auth/registro", registro(correo(), "arrendador"))
                .statusCode());
    }

    private ClienteApi registrarEIniciar(String rol) throws Exception {
        String correo = correo();
        ClienteApi cliente = registrar(correo, rol);
        cliente.iniciarSesion(correo, CLAVE);
        return cliente;
    }

    private ClienteApi registrar(String correo, String rol) throws Exception {
        ClienteApi cliente = new ClienteApi(port, mapper);
        cliente.renovarCsrf();
        var respuesta = cliente.json("POST", "/api/auth/registro", registro(correo, rol));
        assertEquals(201, respuesta.statusCode(), respuesta.body());
        return cliente;
    }

    private Map<String, String> registro(String correo, String rol) {
        return Map.of("nombre", "Ana Prueba", "correo", correo, "contrasena", CLAVE,
                "telefono", "4420000000", "rolSolicitado", rol);
    }

    private Map<String, String> camposPropiedad() {
        Map<String, String> campos = new HashMap<>();
        campos.put("titulo", "Departamento de prueba");
        campos.put("descripcion", "Amplio y bien iluminado");
        campos.put("precioMensual", "12500");
        campos.put("calle", "Av. Universidad");
        campos.put("numeroExterior", "100");
        campos.put("colonia", "Centro");
        campos.put("municipio", "Querétaro");
        campos.put("estadoUbicacion", "Querétaro");
        campos.put("codigoPostal", "76000");
        campos.put("habitaciones", "3");
        campos.put("banos", "2");
        campos.put("estacionamientos", "1");
        campos.put("metrosCuadrados", "90");
        return campos;
    }

    private ClienteApi.Archivo png(String campo) throws Exception {
        BufferedImage imagen = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", salida);
        return new ClienteApi.Archivo(campo, "casa.png", "image/png", salida.toByteArray());
    }

    private long idServicio(JsonNode servicios, String nombre) {
        return StreamSupport.stream(servicios.spliterator(), false)
                .filter(servicio -> nombre.equals(servicio.get("nombre").asText()))
                .findFirst().orElseThrow().get("idServicio").asLong();
    }

    private JsonNode buscar(JsonNode lista, long idPropiedad) {
        JsonNode encontrada = buscarOpcional(lista, idPropiedad);
        if (encontrada == null) {
            throw new AssertionError("La propiedad " + idPropiedad + " no está en la lista");
        }
        return encontrada;
    }

    private JsonNode buscarOpcional(JsonNode lista, long idPropiedad) {
        return StreamSupport.stream(lista.spliterator(), false)
                .filter(nodo -> nodo.get("idPropiedad").asLong() == idPropiedad)
                .findFirst().orElse(null);
    }

    private String correo() {
        return "prop-test-" + UUID.randomUUID() + "@example.com";
    }
}
