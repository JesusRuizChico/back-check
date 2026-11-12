package com.equipo404.arrendamiento.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Orígenes web autorizados para consumir la API y el canal del chat. */
@ConfigurationProperties(prefix = "habitacheck.cors")
public record CorsProperties(List<String> origenesPermitidos) {

    public CorsProperties {
        origenesPermitidos = (origenesPermitidos == null || origenesPermitidos.isEmpty())
                ? List.of("http://localhost:*", "http://127.0.0.1:*")
                : List.copyOf(origenesPermitidos);
    }

    public String[] comoArreglo() {
        return origenesPermitidos.toArray(String[]::new);
    }
}
