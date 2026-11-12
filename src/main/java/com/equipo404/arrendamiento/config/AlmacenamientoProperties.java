package com.equipo404.arrendamiento.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ubicación local de las imágenes subidas y la ruta HTTP con la que se publican.
 */
@ConfigurationProperties(prefix = "habitacheck.almacenamiento")
public record AlmacenamientoProperties(String directorio, String rutaPublica) {

    public AlmacenamientoProperties {
        directorio = (directorio == null || directorio.isBlank()) ? "uploads" : directorio;
        rutaPublica = (rutaPublica == null || rutaPublica.isBlank()) ? "/uploads" : rutaPublica;
    }

    public Path directorioAbsoluto() {
        return Path.of(directorio).toAbsolutePath().normalize();
    }

    public String rutaPublicaNormalizada() {
        String ruta = rutaPublica.startsWith("/") ? rutaPublica : "/" + rutaPublica;
        return ruta.endsWith("/") ? ruta.substring(0, ruta.length() - 1) : ruta;
    }
}
