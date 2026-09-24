package com.equipo404.arrendamiento.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String targetFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            if (targetFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + targetFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(targetFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + targetFileName + ". Please try again!", ex);
        }
    }

    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un archivo de imagen válido.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El tamaño de la imagen no debe exceder los 5 MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("image/jpeg")
                && !contentType.equalsIgnoreCase("image/png")
                && !contentType.equalsIgnoreCase("image/webp")
                && !contentType.equalsIgnoreCase("image/jpg"))) {
            throw new IllegalArgumentException("Formato de archivo no soportado. Los formatos permitidos son: JPG, PNG y WEBP.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();
            if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png") && !lower.endsWith(".webp")) {
                throw new IllegalArgumentException("Extensión de archivo no soportada. Extensiones permitidas: .jpg, .jpeg, .png, .webp");
            }
        }
    }

    public String storeImage(MultipartFile file) {
        validateImageFile(file);
        return storeFile(file);
    }

    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("..")) {
            return;
        }
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName).normalize();
            if (targetLocation.startsWith(this.fileStorageLocation)) {
                Files.deleteIfExists(targetLocation);
            }
        } catch (IOException ignored) {
            // Silenciosamente ignorar errores al eliminar archivos en el sistema local
        }
    }
}
