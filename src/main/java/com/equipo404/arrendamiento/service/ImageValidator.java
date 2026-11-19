package com.equipo404.arrendamiento.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageValidator {

    ValidatedImage validate(MultipartFile file);

    record ValidatedImage(byte[] bytes, String contentType, String extension) {
    }
}
