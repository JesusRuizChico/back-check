package com.equipo404.arrendamiento;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equipo404.arrendamiento.service.ImageValidator.ValidatedImage;
import com.equipo404.arrendamiento.service.impl.FirebaseImageStorage;
import com.equipo404.arrendamiento.service.impl.LocalImageStorage;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FirebaseImageStorageTests {

    private final Bucket bucket = mock(Bucket.class);
    private final Storage cliente = mock(Storage.class);
    private final LocalImageStorage local = mock(LocalImageStorage.class);
    private final FirebaseImageStorage imagenes = new FirebaseImageStorage(bucket, local);

    @Test
    void laMismaRutaPermiteEncontrarYEliminarLaImagenSubida() {
        when(bucket.getName()).thenReturn("habitacheck-pruebas");
        when(bucket.getStorage()).thenReturn(cliente);
        byte[] bytes = {1, 2, 3};

        String url = imagenes.save("perfiles", new ValidatedImage(bytes, "image/png", ".png"));
        assertTrue(url.startsWith("/media/perfiles/"));
        String nombre = url.substring("/media/perfiles/".length());
        String objeto = "habitacheck/perfiles/" + nombre;

        ArgumentCaptor<BlobInfo> datos = ArgumentCaptor.forClass(BlobInfo.class);
        verify(cliente).create(datos.capture(), eq(bytes), any(Storage.BlobTargetOption.class));
        assertEquals(objeto, datos.getValue().getName());
        assertEquals("image/png", datos.getValue().getContentType());

        Blob blob = mock(Blob.class);
        when(bucket.get(objeto)).thenReturn(blob);
        when(blob.getSize()).thenReturn((long) bytes.length);
        when(blob.getContent()).thenReturn(bytes);
        var imagen = imagenes.find("perfiles", nombre).orElseThrow();
        assertArrayEquals(bytes, imagen.bytes());
        assertEquals("image/png", imagen.contentType());

        imagenes.delete(url);
        verify(cliente).delete("habitacheck-pruebas", objeto);
        verifyNoInteractions(local);
    }

    @Test
    void elBorradoDeUnaFotoExistenteSeDelegaAlDisco() {
        imagenes.delete("/uploads/perfiles/foto-anterior.jpg");

        verify(local).delete("/uploads/perfiles/foto-anterior.jpg");
        verifyNoInteractions(bucket);
    }

    @Test
    void lasRutasFueraDeLasImagenesDeLaAppNoConsultanElBucket() {
        assertTrue(imagenes.find("privado", "123e4567-e89b-12d3-a456-426614174000.jpg").isEmpty());
        assertTrue(imagenes.find("perfiles", "../privado.jpg").isEmpty());
        imagenes.delete("/media/privado/123e4567-e89b-12d3-a456-426614174000.jpg");

        verifyNoInteractions(bucket, local);
    }
}
