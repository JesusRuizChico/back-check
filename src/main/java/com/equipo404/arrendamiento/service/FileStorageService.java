package com.equipo404.arrendamiento.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final FirebaseStorageClient firebaseStorageClient;
    private final ImageFileValidator imageFileValidator;

    public FileStorageService(
            FirebaseStorageClient firebaseStorageClient,
            ImageFileValidator imageFileValidator) {
        this.firebaseStorageClient = firebaseStorageClient;
        this.imageFileValidator = imageFileValidator;
    }

    public String storeImage(MultipartFile file) {
        return storeImage(file, "perfiles");
    }

    public String storeImage(MultipartFile file, String folder) {
        ImageFileValidator.ValidatedImage image = imageFileValidator.validate(file);
        return firebaseStorageClient.upload(folder, image);
    }

    public void deleteFile(String downloadUrl) {
        firebaseStorageClient.deleteByDownloadUrl(downloadUrl);
    }
}
