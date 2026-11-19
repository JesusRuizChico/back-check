package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.config.FirebaseStorageConfig;
import com.equipo404.arrendamiento.exception.AlmacenamientoException;
import com.equipo404.arrendamiento.service.ImageStorage;
import com.equipo404.arrendamiento.service.ImageValidator.ValidatedImage;
import com.equipo404.arrendamiento.service.StoredImageReader;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@Conditional(FirebaseStorageConfig.BucketConfigurado.class)
public class FirebaseImageStorage implements ImageStorage, StoredImageReader {

    private static final Logger LOG = LoggerFactory.getLogger(FirebaseImageStorage.class);
    private static final String PREFIJO_OBJETOS = "habitacheck/";
    private static final String PREFIJO_URL = RUTA_PUBLICA + "/";
    private static final Pattern RUTA_IMAGEN = Pattern.compile(
            "(?:perfiles|propiedades)/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.(jpg|png|webp)");

    private final Bucket bucket;
    private final LocalImageStorage localImageStorage;

    public FirebaseImageStorage(Bucket bucket, LocalImageStorage localImageStorage) {
        this.bucket = bucket;
        this.localImageStorage = localImageStorage;
    }

    @Override
    public String save(String folder, ValidatedImage image) {
        String ruta = folder + "/" + UUID.randomUUID() + image.extension();
        if (!RUTA_IMAGEN.matcher(ruta).matches()) {
            throw new IllegalArgumentException("Ruta de imagen no admitida.");
        }
        BlobInfo objeto = BlobInfo.newBuilder(bucket.getName(), PREFIJO_OBJETOS + ruta)
                .setContentType(image.contentType())
                .build();
        try {
            bucket.getStorage().create(objeto, image.bytes(), Storage.BlobTargetOption.doesNotExist());
            return PREFIJO_URL + ruta;
        } catch (StorageException exception) {
            throw new AlmacenamientoException("No se pudo guardar la imagen en Firebase Storage.", exception);
        }
    }

    @Override
    public Optional<StoredImage> find(String folder, String filename) {
        String ruta = folder + "/" + filename;
        Matcher coincidencia = RUTA_IMAGEN.matcher(ruta);
        if (!coincidencia.matches()) {
            return Optional.empty();
        }
        try {
            Blob objeto = bucket.get(PREFIJO_OBJETOS + ruta);
            if (objeto == null || objeto.getSize() > ImageFileValidator.MAX_IMAGE_SIZE_BYTES) {
                return Optional.empty();
            }
            String tipo = coincidencia.group(1);
            String contentType = "image/" + ("jpg".equals(tipo) ? "jpeg" : tipo);
            return Optional.of(new StoredImage(objeto.getContent(), contentType));
        } catch (StorageException exception) {
            if (exception.getCode() == 404) {
                return Optional.empty();
            }
            throw new AlmacenamientoException("No se pudo leer la imagen en Firebase Storage.", exception);
        }
    }

    @Override
    public void delete(String publicPath) {
        if (publicPath == null || !publicPath.startsWith(PREFIJO_URL)) {
            localImageStorage.delete(publicPath);
            return;
        }
        String ruta = publicPath.substring(PREFIJO_URL.length());
        if (!RUTA_IMAGEN.matcher(ruta).matches()) {
            return;
        }
        try {
            bucket.getStorage().delete(bucket.getName(), PREFIJO_OBJETOS + ruta);
        } catch (StorageException exception) {
            LOG.warn("No se pudo eliminar la imagen {} de Firebase Storage", ruta, exception);
        }
    }
}
