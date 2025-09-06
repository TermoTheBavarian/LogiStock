package com.example.ecommercebackend.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileServiceImplTest {

    private LocalFileServiceImpl fileService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        fileService = new LocalFileServiceImpl();
        tempDir = Files.createTempDirectory("upload-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Limpiar archivos temporales
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .map(Path::toFile)
                    .forEach(file -> file.delete());
        }
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // Crear un archivo simulado
        String fileContent = "Test file content";
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test-file.txt",
                "text/plain",
                fileContent.getBytes()
        );

        // Ejecutar el método
        String uploadedFileName = fileService.uploadFile(multipartFile, tempDir.toString());

        // Verificar que el archivo existe
        Path uploadedPath = tempDir.resolve(uploadedFileName);
        assertTrue(Files.exists(uploadedPath));

        // Verificar contenido
        String content = Files.readString(uploadedPath);
        assertEquals(fileContent, content);
    }

    @Test
    void testUploadFile_CreatesDirectoryIfNotExists() throws IOException {
        // Crear un subdirectorio inexistente
        Path newDir = tempDir.resolve("subdir");

        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Hello".getBytes()
        );

        String uploadedFileName = fileService.uploadFile(multipartFile, newDir.toString());

        Path uploadedPath = newDir.resolve(uploadedFileName);
        assertTrue(Files.exists(uploadedPath));
    }

    @Test
    void testUploadFile_WithDifferentExtension() throws IOException {
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "PNGDATA".getBytes()
        );

        String uploadedFileName = fileService.uploadFile(multipartFile, tempDir.toString());

        // Verificar que el archivo mantiene la extensión .png
        assertTrue(uploadedFileName.endsWith(".png"));
        assertTrue(Files.exists(tempDir.resolve(uploadedFileName)));
    }
}
