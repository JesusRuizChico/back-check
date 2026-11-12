package com.equipo404.arrendamiento.support;

import java.io.ByteArrayOutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.ObjectMapper;

/** Cliente HTTP de pruebas que conserva cookies de sesión y el token CSRF. */
public class ClienteApi {

    public record Archivo(String campo, String nombre, String tipo, byte[] contenido) {
    }

    private final int puerto;
    private final ObjectMapper mapper;
    private final HttpClient http;
    private String token;

    public ClienteApi(int puerto, ObjectMapper mapper) {
        this.puerto = puerto;
        this.mapper = mapper;
        this.http = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();
    }

    public HttpResponse<String> json(String metodo, String ruta, Object cuerpo) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(ruta))
                .header("Content-Type", "application/json");
        agregarCsrf(request);
        request.method(metodo, cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)));
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> get(String ruta) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(ruta)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<byte[]> getBytes(String ruta) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(ruta)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    public HttpResponse<String> multipart(String ruta, Map<String, String> campos, Archivo... archivos)
            throws Exception {
        String limite = "----habitacheck" + UUID.randomUUID();
        ByteArrayOutputStream cuerpo = new ByteArrayOutputStream();
        for (Map.Entry<String, String> campo : campos.entrySet()) {
            escribir(cuerpo, "--" + limite + "\r\n"
                    + "Content-Disposition: form-data; name=\"" + campo.getKey() + "\"\r\n\r\n"
                    + campo.getValue() + "\r\n");
        }
        for (Archivo archivo : archivos) {
            escribir(cuerpo, "--" + limite + "\r\n"
                    + "Content-Disposition: form-data; name=\"" + archivo.campo() + "\"; filename=\""
                    + archivo.nombre() + "\"\r\n"
                    + "Content-Type: " + archivo.tipo() + "\r\n\r\n");
            cuerpo.write(archivo.contenido());
            escribir(cuerpo, "\r\n");
        }
        escribir(cuerpo, "--" + limite + "--\r\n");
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(ruta))
                .header("Content-Type", "multipart/form-data; boundary=" + limite)
                .POST(HttpRequest.BodyPublishers.ofByteArray(cuerpo.toByteArray()));
        agregarCsrf(request);
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    public void renovarCsrf() throws Exception {
        HttpResponse<String> respuesta = get("/api/csrf");
        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("No se pudo obtener CSRF: " + respuesta.statusCode());
        }
        token = mapper.readTree(respuesta.body()).get("token").asText();
    }

    public void iniciarSesion(String correo, String contrasena) throws Exception {
        renovarCsrf();
        HttpResponse<String> respuesta = json("POST", "/api/auth/login",
                Map.of("correo", correo, "contrasena", contrasena));
        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("Login fallido: " + respuesta.body());
        }
        renovarCsrf();
    }

    private void agregarCsrf(HttpRequest.Builder request) {
        if (token != null) {
            request.header("X-XSRF-TOKEN", token);
        }
    }

    private URI uri(String ruta) {
        return URI.create("http://localhost:" + puerto + ruta);
    }

    private static void escribir(ByteArrayOutputStream salida, String texto) {
        salida.writeBytes(texto.getBytes(StandardCharsets.UTF_8));
    }
}
