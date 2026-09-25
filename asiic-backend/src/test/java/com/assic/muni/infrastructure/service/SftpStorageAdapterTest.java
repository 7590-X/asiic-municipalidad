package com.assic.muni.infrastructure.service;

import com.assic.muni.infrastructure.config.SftpProperties;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.integration.file.remote.SessionCallback;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SftpStorageAdapter - Pruebas Unitarias")
class SftpStorageAdapterTest {

    @Mock
    private SftpRemoteFileTemplate sftpRemoteFileTemplate;

    @Mock
    private SftpProperties sftpProperties;

    @Mock
    private Session session;

    @InjectMocks
    private SftpStorageAdapter sftpStorageAdapter;

    private static final String BASE_DIR = "/archivos/asiic";

    @BeforeEach
    void setUp() {
        lenient().when(sftpProperties.getRemoteDirectory()).thenReturn(BASE_DIR);
    }

    @Nested
    @DisplayName("Operaciones de subida (Upload)")
    class UploadScenarios {

        @Test
        @DisplayName("Debe subir un archivo exitosamente con InputStream")
        void shouldUploadFileWithInputStreamSuccessfully() throws IOException {
            // Arrange
            String filename = "evidencia1.png";
            String subDir = "quejas";
            InputStream stream = new ByteArrayInputStream("contenido".getBytes(StandardCharsets.UTF_8));

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(anyString())).thenReturn(true);

            // Act
            String remotePath = sftpStorageAdapter.uploadFile(stream, filename, subDir);

            // Assert
            assertThat(remotePath).isEqualTo(BASE_DIR + "/quejas/" + filename);
            verify(session, times(1)).write(any(InputStream.class), eq(remotePath));
        }

        @Test
        @DisplayName("Debe subir un archivo exitosamente a partir de bytes")
        void shouldUploadFileWithBytesSuccessfully() throws IOException {
            // Arrange
            String filename = "doc.pdf";
            byte[] data = "pdf-data".getBytes(StandardCharsets.UTF_8);

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(anyString())).thenReturn(true);

            // Act
            String remotePath = sftpStorageAdapter.uploadFile(data, filename, null);

            // Assert
            assertThat(remotePath).isEqualTo(BASE_DIR + "/" + filename);
            verify(session, times(1)).write(any(InputStream.class), eq(remotePath));
        }

        @Test
        @DisplayName("Debe subir un MultipartFile generando nombre único sanitizado")
        void shouldUploadMultipartFileSuccessfully() throws IOException {
            // Arrange
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", "foto_perfil.jpeg", "image/jpeg", "imagen-binaria".getBytes(StandardCharsets.UTF_8));

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(anyString())).thenReturn(true);

            // Act
            String remotePath = sftpStorageAdapter.uploadFile(multipartFile, "fotos");

            // Assert
            assertThat(remotePath).startsWith(BASE_DIR + "/fotos/");
            assertThat(remotePath).endsWith(".jpeg");
            verify(session, times(1)).write(any(InputStream.class), eq(remotePath));
        }

        @Test
        @DisplayName("Debe lanzar BAD_REQUEST si el InputStream es nulo")
        void shouldThrowBadRequestWhenInputStreamIsNull() {
            assertThatThrownBy(() -> sftpStorageAdapter.uploadFile((InputStream) null, "test.txt", null))
                    .isInstanceOf(InfrastructureException.class)
                    .hasMessageContaining("El stream del archivo no puede ser nulo")
                    .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Debe lanzar BAD_REQUEST si el MultipartFile está vacío")
        void shouldThrowBadRequestWhenMultipartFileIsEmpty() {
            MockMultipartFile emptyFile = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]);

            assertThatThrownBy(() -> sftpStorageAdapter.uploadFile(emptyFile, "sub"))
                    .isInstanceOf(InfrastructureException.class)
                    .hasMessageContaining("El archivo proporcionado está vacío")
                    .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Operaciones de descarga (Download)")
    class DownloadScenarios {

        @Test
        @DisplayName("Debe descargar un archivo existente como arreglo de bytes")
        void shouldDownloadFileSuccessfully() throws IOException {
            // Arrange
            String remotePath = "/archivos/asiic/quejas/doc.pdf";
            byte[] fileBytes = "contenido-del-documento".getBytes(StandardCharsets.UTF_8);

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(remotePath)).thenReturn(true);
            doAnswer(invocation -> {
                OutputStream os = invocation.getArgument(1);
                os.write(fileBytes);
                return null;
            }).when(session).read(eq(remotePath), any(OutputStream.class));

            // Act
            byte[] downloaded = sftpStorageAdapter.downloadFile(remotePath);

            // Assert
            assertThat(downloaded).isEqualTo(fileBytes);
            verify(session, times(1)).read(eq(remotePath), any(OutputStream.class));
        }

        @Test
        @DisplayName("Debe lanzar NOT_FOUND si el archivo a descargar no existe en el servidor SFTP")
        void shouldThrowNotFoundWhenFileDoesNotExist() throws IOException {
            // Arrange
            String remotePath = "/archivos/asiic/inexistente.pdf";

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(remotePath)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> sftpStorageAdapter.downloadFile(remotePath))
                    .isInstanceOf(InfrastructureException.class)
                    .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe retornar un InputStream válido al llamar a downloadFileInputStream")
        void shouldDownloadFileInputStreamSuccessfully() throws IOException {
            // Arrange
            String remotePath = "/archivos/asiic/stream.txt";
            byte[] fileBytes = "flujo-de-datos".getBytes(StandardCharsets.UTF_8);

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(remotePath)).thenReturn(true);
            doAnswer(invocation -> {
                OutputStream os = invocation.getArgument(1);
                os.write(fileBytes);
                return null;
            }).when(session).read(eq(remotePath), any(OutputStream.class));

            // Act
            InputStream is = sftpStorageAdapter.downloadFileInputStream(remotePath);

            // Assert
            assertThat(is).isNotNull();
            assertThat(is.readAllBytes()).isEqualTo(fileBytes);
        }
    }

    @Nested
    @DisplayName("Operaciones de eliminación y existencia (Delete & Exists)")
    class DeleteAndExistsScenarios {

        @Test
        @DisplayName("Debe eliminar un archivo si existe y retornar true")
        void shouldDeleteExistingFileSuccessfully() throws IOException {
            // Arrange
            String remotePath = "/archivos/asiic/eliminar.pdf";

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(remotePath)).thenReturn(true);
            when(session.remove(remotePath)).thenReturn(true);

            // Act
            boolean result = sftpStorageAdapter.deleteFile(remotePath);

            // Assert
            assertThat(result).isTrue();
            verify(session, times(1)).remove(remotePath);
        }

        @Test
        @DisplayName("Debe retornar false si se intenta eliminar un archivo que no existe")
        void shouldReturnFalseWhenDeletingNonExistingFile() throws IOException {
            // Arrange
            String remotePath = "/archivos/asiic/no-existe.pdf";

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(remotePath)).thenReturn(false);

            // Act
            boolean result = sftpStorageAdapter.deleteFile(remotePath);

            // Assert
            assertThat(result).isFalse();
            verify(session, never()).remove(remotePath);
        }

        @Test
        @DisplayName("Debe verificar si un archivo existe en SFTP")
        void shouldCheckFileExistsCorrectly() throws IOException {
            // Arrange
            String remotePath = "/archivos/asiic/existe.pdf";

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(remotePath)).thenReturn(true);

            // Act
            boolean exists = sftpStorageAdapter.exists(remotePath);

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Debe listar archivos filtrando . y ..")
        void shouldListFilesExcludingRelativeNavigators() throws IOException {
            // Arrange
            String subDir = "carpeta";
            String fullDir = BASE_DIR + "/carpeta";

            when(sftpRemoteFileTemplate.execute(any())).thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                return callback.doInSession(session);
            });
            when(session.exists(fullDir)).thenReturn(true);
            when(session.listNames(fullDir)).thenReturn(new String[]{".", "..", "archivo1.pdf", "archivo2.png"});

            // Act
            List<String> files = sftpStorageAdapter.listFiles(subDir);

            // Assert
            assertThat(files).containsExactly("archivo1.pdf", "archivo2.png");
        }
    }
}
