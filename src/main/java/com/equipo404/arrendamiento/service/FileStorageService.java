package com.equipo404.arrendamiento.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /** Valida y guarda la imagen; devuelve la ruta pública con la que se sirve. */
    String storeImage(MultipartFile file, String folder);

    /** Elimina la imagen indicada por su ruta pública. Ignora rutas ajenas o nulas. */
    void deleteFile(String publicPath);
}
