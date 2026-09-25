package com.assic.muni.infrastructure.service;

import com.assic.muni.application.port.out.SftpStoragePort;
import com.assic.muni.domain.model.AsArchivo;
import com.assic.muni.domain.repository.AsArchivoRepository;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SftpFileStorageAdapter - Pruebas Unitarias")
class SftpFileStorageAdapterTest {

    @Mock
    private SftpStoragePort sftpStoragePort;

    @Mock
    private AsArchivoRepository asArchivoRepository;

    @InjectMocks
    private SftpFileStorageAdapter sftpFileStorageAdapter;

    @Captor
    private ArgumentCaptor<AsArchivo> archivoCaptor;

    @Nested
    @DisplayName("Almacenamiento (storeFile)")
    class StoreFileScenarios {

        @Test
        @DisplayName("Debe subir archivo a SFTP y persistir entidad AsArchivo correctamente")
        void shouldStoreFileInSftpAndSaveEntity() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file", "evidencia.png", "image/png", "bytes-evidencia".getBytes(StandardCharsets.UTF_8));
            String expectedRemotePath = "/archivos/asiic/evidencias/unique-hash.png";

            when(sftpStoragePort.uploadFile(any(InputStream.class), anyString(), any()))
                    .thenReturn(expectedRemotePath);
            when(asArchivoRepository.save(any(AsArchivo.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AsArchivo result = sftpFileStorageAdapter.storeFile(file);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getArNombre()).isEqualTo("evidencia.png");
            assertThat(result.getArPath()).isEqualTo(expectedRemotePath);
            assertThat(result.getArFormato()).isEqualTo("png");
            assertThat(result.getArHash()).isNotEmpty();

            verify(asArchivoRepository, times(1)).save(archivoCaptor.capture());
            AsArchivo captured = archivoCaptor.getValue();
            assertThat(captured.getArPath()).isEqualTo(expectedRemotePath);
        }

        @Test
        @DisplayName("Debe lanzar BAD_REQUEST si el archivo está vacío")
        void shouldThrowBadRequestWhenFileIsEmpty() {
            MockMultipartFile emptyFile = new MockMultipartFile("file", "vacio.txt", "text/plain", new byte[0]);

            assertThatThrownBy(() -> sftpFileStorageAdapter.storeFile(emptyFile))
                    .isInstanceOf(InfrastructureException.class)
                    .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

            verifyNoInteractions(sftpStoragePort);
            verifyNoInteractions(asArchivoRepository);
        }
    }

    @Nested
    @DisplayName("Obtención (getFile)")
    class GetFileScenarios {

        @Test
        @DisplayName("Debe recuperar bytes de archivo desde SFTP si existe en la BD")
        void shouldGetFileBytesSuccessfully() {
            // Arrange
            Integer archivoId = 42;
            AsArchivo archivo = new AsArchivo();
            archivo.setId(archivoId);
            archivo.setArPath("/archivos/asiic/evidencias/foto.png");

            byte[] expectedBytes = "contenido-binario".getBytes(StandardCharsets.UTF_8);

            when(asArchivoRepository.findById(archivoId)).thenReturn(Optional.of(archivo));
            when(sftpStoragePort.downloadFile(archivo.getArPath())).thenReturn(expectedBytes);

            // Act
            byte[] actualBytes = sftpFileStorageAdapter.getFile(archivoId);

            // Assert
            assertThat(actualBytes).isEqualTo(expectedBytes);
            verify(sftpStoragePort, times(1)).downloadFile(archivo.getArPath());
        }

        @Test
        @DisplayName("Debe lanzar NOT_FOUND si el registro no existe en la BD")
        void shouldThrowNotFoundWhenEntityDoesNotExist() {
            // Arrange
            Integer archivoId = 99;
            when(asArchivoRepository.findById(archivoId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> sftpFileStorageAdapter.getFile(archivoId))
                    .isInstanceOf(InfrastructureException.class)
                    .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

            verifyNoInteractions(sftpStoragePort);
        }
    }

    @Nested
    @DisplayName("Eliminación (deleteFile)")
    class DeleteFileScenarios {

        @Test
        @DisplayName("Debe eliminar el archivo en SFTP y borrar el registro en base de datos")
        void shouldDeleteFileFromSftpAndRepository() {
            // Arrange
            Integer archivoId = 15;
            AsArchivo archivo = new AsArchivo();
            archivo.setId(archivoId);
            archivo.setArPath("/archivos/asiic/evidencias/delete_me.pdf");

            when(asArchivoRepository.findById(archivoId)).thenReturn(Optional.of(archivo));
            when(sftpStoragePort.deleteFile(archivo.getArPath())).thenReturn(true);

            // Act
            boolean deleted = sftpFileStorageAdapter.deleteFile(archivoId);

            // Assert
            assertThat(deleted).isTrue();
            verify(sftpStoragePort, times(1)).deleteFile(archivo.getArPath());
            verify(asArchivoRepository, times(1)).delete(archivo);
        }

        @Test
        @DisplayName("Debe retornar false si el archivo a eliminar no existe en BD")
        void shouldReturnFalseWhenEntityDoesNotExistForDeletion() {
            // Arrange
            Integer archivoId = 88;
            when(asArchivoRepository.findById(archivoId)).thenReturn(Optional.empty());

            // Act
            boolean deleted = sftpFileStorageAdapter.deleteFile(archivoId);

            // Assert
            assertThat(deleted).isFalse();
            verifyNoInteractions(sftpStoragePort);
            verify(asArchivoRepository, never()).delete(any());
        }
    }
}
