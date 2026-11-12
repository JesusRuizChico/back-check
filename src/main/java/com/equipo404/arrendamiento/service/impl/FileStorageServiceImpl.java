package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.service.FileStorageService;
import com.equipo404.arrendamiento.service.ImageStorage;
import com.equipo404.arrendamiento.service.ImageValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final ImageValidator imageValidator;
    private final ImageStorage imageStorage;

    public FileStorageServiceImpl(ImageValidator imageValidator, ImageStorage imageStorage) {
        this.imageValidator = imageValidator;
        this.imageStorage = imageStorage;
    }

    @Override
    public String storeImage(MultipartFile file, String folder) {
        return imageStorage.save(folder, imageValidator.validate(file));
    }

    @Override
    public void deleteFile(String publicPath) {
        imageStorage.delete(publicPath);
    }
}
