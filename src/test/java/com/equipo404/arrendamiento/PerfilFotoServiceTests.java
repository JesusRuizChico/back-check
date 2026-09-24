package com.equipo404.arrendamiento;

import com.equipo404.arrendamiento.dto.response.UsuarioResponse;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.repository.UsuarioRolRepository;
import com.equipo404.arrendamiento.service.FileStorageService;
import com.equipo404.arrendamiento.service.PerfilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilFotoServiceTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PerfilService perfilService;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setIdUsuario(1L);
        usuarioPrueba.setNombre("Abigail Hernández");
        usuarioPrueba.setCorreo("abigail@example.com");
        usuarioPrueba.setEstado("activo");
        usuarioPrueba.setFotoPerfil("http://localhost:8080/uploads/foto-antigua.jpg");
    }

    @Test
    @DisplayName("Escenario 1: Actualización exitosa de la foto de perfil")
    void actualizarFotoPerfil_Exitoso() {
        MockMultipartFile archivoImagen = new MockMultipartFile(
                "foto",
                "nueva-foto.png",
                "image/png",
                "contenido-imagen-bytes".getBytes()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(fileStorageService.storeImage(archivoImagen)).thenReturn("uuid-nueva-foto.png");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRolRepository.findByUsuario(any(Usuario.class))).thenReturn(Collections.emptyList());

        UsuarioResponse response = perfilService.actualizarFotoPerfil(1L, archivoImagen);

        assertNotNull(response);
        assertNotNull(response.getFotoPerfil());
        assertTrue(response.getFotoPerfil().contains("uuid-nueva-foto.png"));
        verify(fileStorageService).deleteFile("foto-antigua.jpg");
        verify(usuarioRepository).save(usuarioPrueba);
    }

    @Test
    @DisplayName("Escenario 2: Intento de subir archivo no soportado (texto/pdf)")
    void actualizarFotoPerfil_ArchivoNoSoportado() {
        MockMultipartFile archivoInvalido = new MockMultipartFile(
                "foto",
                "documento.pdf",
                "application/pdf",
                "contenido-pdf".getBytes()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        doThrow(new IllegalArgumentException("Formato de archivo no soportado. Los formatos permitidos son: JPG, PNG y WEBP."))
                .when(fileStorageService).storeImage(archivoInvalido);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                perfilService.actualizarFotoPerfil(1L, archivoInvalido)
        );

        assertTrue(ex.getMessage().contains("no soportado"));
        assertEquals("http://localhost:8080/uploads/foto-antigua.jpg", usuarioPrueba.getFotoPerfil());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 2: Intento de subir archivo que excede el tamaño permitido")
    void actualizarFotoPerfil_TamanioExcedido() {
        MockMultipartFile archivoPesado = new MockMultipartFile(
                "foto",
                "foto-gigante.png",
                "image/png",
                new byte[6 * 1024 * 1024]
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        doThrow(new IllegalArgumentException("El tamaño de la imagen no debe exceder los 5 MB."))
                .when(fileStorageService).storeImage(archivoPesado);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                perfilService.actualizarFotoPerfil(1L, archivoPesado)
        );

        assertTrue(ex.getMessage().contains("no debe exceder los 5 MB"));
        assertEquals("http://localhost:8080/uploads/foto-antigua.jpg", usuarioPrueba.getFotoPerfil());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 3: Eliminación de la foto de perfil")
    void eliminarFotoPerfil_Exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRolRepository.findByUsuario(any(Usuario.class))).thenReturn(Collections.emptyList());

        UsuarioResponse response = perfilService.eliminarFotoPerfil(1L);

        assertNotNull(response);
        assertNull(response.getFotoPerfil(), "La foto de perfil debe ser null para que se muestre el avatar predeterminado");
        verify(fileStorageService).deleteFile("foto-antigua.jpg");
        verify(usuarioRepository).save(usuarioPrueba);
    }
}
