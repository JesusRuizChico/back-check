package com.equipo404.arrendamiento;

import com.equipo404.arrendamiento.service.ImageFileValidator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageFileValidatorTests {

    private final ImageFileValidator validator = new ImageFileValidator();

    @Test
    void acceptsValidPngWithinLimit() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "casa.png", "image/png", validPng());

        ImageFileValidator.ValidatedImage result = validator.validate(file);

        assertEquals("image/png", result.contentType());
        assertEquals(".png", result.extension());
    }

    @Test
    void rejectsContentThatIsNotAnImageEvenWhenNameAndMimeSayPng() {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "casa.png", "image/png", "no es una imagen".getBytes());

        assertThrows(IllegalArgumentException.class, () -> validator.validate(file));
    }

    @Test
    void rejectsMimeExtensionMismatch() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "casa.jpg", "image/png", validPng());

        assertThrows(IllegalArgumentException.class, () -> validator.validate(file));
    }

    @Test
    void rejectsImagesLargerThanFiveMegabytes() {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "casa.png", "image/png",
                new byte[(int) ImageFileValidator.MAX_IMAGE_SIZE_BYTES + 1]);

        assertThrows(IllegalArgumentException.class, () -> validator.validate(file));
    }

    private byte[] validPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
