package com.equipo404.arrendamiento.service;

import java.util.Optional;

public interface StoredImageReader {

    String RUTA_PUBLICA = "/media";

    Optional<StoredImage> find(String folder, String filename);

    record StoredImage(byte[] bytes, String contentType) {
    }
}
