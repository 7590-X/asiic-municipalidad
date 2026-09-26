package com.assic.muni.domain.event;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record RegistrarEvidenciasEvent(
        int incidenciaId,
        List<MultipartFile> evidencias
) {
}
