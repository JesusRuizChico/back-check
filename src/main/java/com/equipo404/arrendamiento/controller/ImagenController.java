package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.config.FirebaseStorageConfig;
import com.equipo404.arrendamiento.service.StoredImageReader;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Conditional(FirebaseStorageConfig.BucketConfigurado.class)
public class ImagenController {

    private final StoredImageReader imagenes;

    public ImagenController(StoredImageReader imagenes) {
        this.imagenes = imagenes;
    }

    @GetMapping(StoredImageReader.RUTA_PUBLICA + "/{folder}/{filename}")
    public ResponseEntity<byte[]> obtener(@PathVariable String folder, @PathVariable String filename) {
        return imagenes.find(folder, filename)
                .map(imagen -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(imagen.contentType()))
                        .body(imagen.bytes()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
