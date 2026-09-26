package com.assic.muni.infrastructure.listener;

import com.assic.muni.application.port.out.FileStoragePort;
import com.assic.muni.domain.event.RegistrarEvidenciasEvent;
import com.assic.muni.domain.model.AsArchivo;
import com.assic.muni.domain.model.AsIncidenciaArchivo;
import com.assic.muni.domain.model.AsIncidenciaArchivoId;
import com.assic.muni.domain.repository.AsIncidenciaArchivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArchivoEventListener {

    private final AsIncidenciaArchivoRepository incidenciaArchivoRepository;
    private final FileStoragePort fileStoragePort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void eventRegistrarEvidencias(RegistrarEvidenciasEvent event) {
        if (event.evidencias() != null && !event.evidencias().isEmpty()) {
            for (MultipartFile mf : event.evidencias()) {
                AsArchivo archivo = fileStoragePort.storeFile(mf);
                incidenciaArchivoRepository.save(
                        AsIncidenciaArchivo.builder()
                                .id(new AsIncidenciaArchivoId(archivo.getId(), event.incidenciaId()))
                                .aiEstado("A")
                                .build()
                );
            }
        }
    }
}
