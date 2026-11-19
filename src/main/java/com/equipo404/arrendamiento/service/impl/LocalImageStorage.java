package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.config.AlmacenamientoProperties;
import com.equipo404.arrendamiento.exception.AlmacenamientoException;
import com.equipo404.arrendamiento.service.ImageStorage;
import com.equipo404.arrendamiento.service.ImageValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Guarda las imágenes en el sistema de archivos del servidor y las expone
 * bajo la ruta pública configurada (por defecto /uploads).
 */
@Component
public class LocalImageStorage implements ImageStorage {

    private static final Logger LOG = LoggerFactory.getLogger(LocalImageStorage.class);

    private final Path directorioBase;
    private final String rutaPublica;

    public LocalImageStorage(AlmacenamientoProperties propiedades) {
        this.directorioBase = propiedades.directorioAbsoluto();
        this.rutaPublica = propiedades.rutaPublicaNormalizada();
    }

    @Override
    public String save(String folder, ImageValidator.ValidatedImage image) {
        String carpetaSegura = folder.replaceAll("[^a-zA-Z0-9_-]", "");
        String nombreArchivo = UUID.randomUUID() + image.extension();
        Path carpeta = directorioBase.resolve(carpetaSegura);
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(nombreArchivo), image.bytes(),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new AlmacenamientoException("No se pudo guardar la imagen en el servidor.", exception);
        }
        return rutaPublica + "/" + carpetaSegura + "/" + nombreArchivo;
    }

    @Override
    public void delete(String publicPath) {
        if (publicPath == null || !publicPath.startsWith(rutaPublica + "/")) {
            return;
        }
        Path archivo = directorioBase.resolve(publicPath.substring(rutaPublica.length() + 1)).normalize();
        if (!archivo.startsWith(directorioBase)) {
            return;
        }
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException exception) {
            LOG.warn("No se pudo eliminar la imagen {}", publicPath, exception);
        }
    }
}
