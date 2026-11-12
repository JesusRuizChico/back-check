package com.equipo404.arrendamiento.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Publica las imágenes guardadas localmente para que el frontend pueda mostrarlas. */
@Configuration
public class ArchivosPublicosConfig implements WebMvcConfigurer {

    private final AlmacenamientoProperties propiedades;

    public ArchivosPublicosConfig(AlmacenamientoProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(propiedades.rutaPublicaNormalizada() + "/**")
                .addResourceLocations(propiedades.directorioAbsoluto().toUri().toString());
    }
}
