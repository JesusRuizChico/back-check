package com.equipo404.arrendamiento.service;

/** Destino físico de las imágenes ya validadas. */
public interface ImageStorage {

    String save(String folder, ImageValidator.ValidatedImage image);

    void delete(String publicPath);
}
