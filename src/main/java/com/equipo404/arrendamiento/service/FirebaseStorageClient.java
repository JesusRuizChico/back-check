package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.exception.FirebaseStorageUnavailableException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Component
public class FirebaseStorageClient {

    private static final String APP_NAME = "habitacheck-storage";
    private static final String DOWNLOAD_URL_HOST = "firebasestorage.googleapis.com";
    private static final String DOWNLOAD_TOKEN_METADATA = "firebaseStorageDownloadTokens";

    private final String projectId;
    private final String bucketName;
    private volatile Bucket bucket;

    public FirebaseStorageClient(
            @Value("${habitacheck.firebase.project-id}") String projectId,
            @Value("${habitacheck.firebase.storage-bucket}") String bucketName) {
        this.projectId = projectId;
        this.bucketName = bucketName.replaceFirst("^gs://", "");
    }

    public String upload(String folder, ImageFileValidator.ValidatedImage image) {
        String safeFolder = folder.replaceAll("[^a-zA-Z0-9_-]", "");
        String objectName = safeFolder + "/" + UUID.randomUUID() + image.extension();
        String downloadToken = UUID.randomUUID().toString();

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName))
                    .setContentType(image.contentType())
                    .setCacheControl("public, max-age=31536000, immutable")
                    .setMetadata(Map.of(DOWNLOAD_TOKEN_METADATA, downloadToken))
                    .build();

            getBucket().getStorage().create(blobInfo, image.bytes());
            String encodedObjectName = URLEncoder.encode(objectName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return "https://" + DOWNLOAD_URL_HOST + "/v0/b/" + bucketName
                    + "/o/" + encodedObjectName
                    + "?alt=media&token=" + downloadToken;
        } catch (RuntimeException exception) {
            if (exception instanceof FirebaseStorageUnavailableException storageException) {
                throw storageException;
            }
            throw new FirebaseStorageUnavailableException(
                    "No fue posible guardar la imagen en Firebase Storage.", exception);
        }
    }

    public void deleteByDownloadUrl(String downloadUrl) {
        String objectName = extractObjectName(downloadUrl);
        if (objectName == null) {
            return;
        }

        try {
            getBucket().getStorage().delete(BlobId.of(bucketName, objectName));
        } catch (RuntimeException exception) {
            org.slf4j.LoggerFactory.getLogger(FirebaseStorageClient.class)
                    .warn("No se pudo borrar el objeto de Firebase Storage.", exception);
        }
    }

    private String extractObjectName(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(downloadUrl);
            String expectedPrefix = "/v0/b/" + bucketName + "/o/";
            if (!DOWNLOAD_URL_HOST.equalsIgnoreCase(uri.getHost())
                    || uri.getRawPath() == null
                    || !uri.getRawPath().startsWith(expectedPrefix)) {
                return null;
            }
            return URLDecoder.decode(
                    uri.getRawPath().substring(expectedPrefix.length()),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Bucket getBucket() {
        Bucket currentBucket = bucket;
        if (currentBucket != null) {
            return currentBucket;
        }

        synchronized (this) {
            if (bucket != null) {
                return bucket;
            }

            try {
                FirebaseApp app = FirebaseApp.getApps().stream()
                        .filter(existing -> APP_NAME.equals(existing.getName()))
                        .findFirst()
                        .orElseGet(this::initializeFirebaseApp);
                bucket = StorageClient.getInstance(app).bucket(bucketName);
                return bucket;
            } catch (FirebaseStorageUnavailableException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new FirebaseStorageUnavailableException(
                        "No se pudo conectar con Firebase Storage.", exception);
            }
        }
    }

    private FirebaseApp initializeFirebaseApp() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId(projectId)
                    .setStorageBucket(bucketName)
                    .build();
            return FirebaseApp.initializeApp(options, APP_NAME);
        } catch (IOException | RuntimeException exception) {
            throw new FirebaseStorageUnavailableException(
                    "Configura las credenciales de Firebase del servidor antes de subir imágenes.",
                    exception);
        }
    }
}
