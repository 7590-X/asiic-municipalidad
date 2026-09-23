package com.assic.muni.application.cqrs.cmd;

import com.assic.muni.application.cqrs.dto.IncidenciaPayloadDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Getter
@AllArgsConstructor
public class ActualizarIncidenciaCmd {
    private String incidenciaId;
    private String userId;
    private IncidenciaPayloadDto payload;
    private List<MultipartFile> evidenciasNuevas;
}
