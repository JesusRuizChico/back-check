package com.equipo404.arrendamiento.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

@Component
public class ImageFileValidator {

    public static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una imagen.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Cada imagen debe pesar como máximo 5 MB.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo leer la imagen.", exception);
        }

        ImageType imageType = identify(bytes);
        if (imageType == null) {
            throw new IllegalArgumentException("El contenido no corresponde a JPG, PNG o WEBP.");
        }

        String extension = extension(file.getOriginalFilename());
        if (!imageType.acceptsExtension(extension)) {
            throw new IllegalArgumentException("La extensión del archivo no coincide con su contenido.");
        }

        String declaredType = normalizeContentType(file.getContentType());
        if (declaredType != null
                && !"application/octet-stream".equals(declaredType)
                && !imageType.contentType.equals(declaredType)) {
            throw new IllegalArgumentException("El tipo MIME no coincide con el contenido de la imagen.");
        }

        if (imageType != ImageType.WEBP) {
            validateDecodableImage(bytes);
        } else {
            validateWebpContainer(bytes);
        }

        return new ValidatedImage(bytes, imageType.contentType, imageType.canonicalExtension);
    }

    private ImageType identify(byte[] bytes) {
        if (startsWith(bytes, 0xFF, 0xD8, 0xFF)) {
            return ImageType.JPEG;
        }
        if (startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return ImageType.PNG;
        }
        if (bytes.length >= 12
                && asciiEquals(bytes, 0, "RIFF")
                && asciiEquals(bytes, 8, "WEBP")) {
            return ImageType.WEBP;
        }
        return null;
    }

    private void validateDecodableImage(byte[] bytes) {
        ImageReader reader = null;
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) {
                throw invalidImage();
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            reader = readers.next();
            reader.setInput(input, true, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            if (width <= 0 || height <= 0 || (long) width * height > MAX_IMAGE_PIXELS) {
                throw new IllegalArgumentException("La imagen tiene dimensiones no permitidas.");
            }
            BufferedImage decoded = reader.read(0);
            if (decoded == null) {
                throw invalidImage();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("El archivo de imagen está dañado.", exception);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }

    private void validateWebpContainer(byte[] bytes) {
        if (bytes.length < 20 || readLittleEndianInt(bytes, 4) != bytes.length - 8L) {
            throw invalidImage();
        }

        String chunk = new String(bytes, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
        long chunkSize = readLittleEndianInt(bytes, 16);
        if (!("VP8 ".equals(chunk) || "VP8L".equals(chunk) || "VP8X".equals(chunk))
                || chunkSize < 1
                || chunkSize > bytes.length - 20L) {
            throw invalidImage();
        }
    }

    private long readLittleEndianInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFFL)
                | ((bytes[offset + 1] & 0xFFL) << 8)
                | ((bytes[offset + 2] & 0xFFL) << 16)
                | ((bytes[offset + 3] & 0xFFL) << 24);
    }

    private String extension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo debe tener extensión de imagen.");
        }
        String safeName = originalFilename.replace('\\', '/');
        String baseName = safeName.substring(safeName.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
        int dot = baseName.lastIndexOf('.');
        if (dot < 0 || dot == baseName.length() - 1) {
            throw new IllegalArgumentException("El archivo debe terminar en .jpg, .jpeg, .png o .webp.");
        }
        String extension = baseName.substring(dot + 1);
        if (!("jpg".equals(extension) || "jpeg".equals(extension)
                || "png".equals(extension) || "webp".equals(extension))) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG, PNG o WEBP.");
        }
        return extension;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        return "image/jpg".equals(normalized) ? "image/jpeg" : normalized;
    }

    private boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean asciiEquals(byte[] bytes, int offset, String value) {
        if (bytes.length < offset + value.length()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (bytes[offset + i] != (byte) value.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private IllegalArgumentException invalidImage() {
        return new IllegalArgumentException("El archivo de imagen está dañado.");
    }

    private enum ImageType {
        JPEG("image/jpeg", ".jpg"),
        PNG("image/png", ".png"),
        WEBP("image/webp", ".webp");

        private final String contentType;
        private final String canonicalExtension;

        ImageType(String contentType, String canonicalExtension) {
            this.contentType = contentType;
            this.canonicalExtension = canonicalExtension;
        }

        private boolean acceptsExtension(String extension) {
            return switch (this) {
                case JPEG -> "jpg".equals(extension) || "jpeg".equals(extension);
                case PNG -> "png".equals(extension);
                case WEBP -> "webp".equals(extension);
            };
        }
    }

    public record ValidatedImage(byte[] bytes, String contentType, String extension) {
    }
}
