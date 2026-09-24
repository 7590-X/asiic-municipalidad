package com.assic.muni.application.mapper;

import com.assic.muni.application.cqrs.dto.IncidenciaQuejaDto;
import com.assic.muni.domain.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IncidenciaMapper - Pruebas Unitarias")
class IncidenciaMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Debe mapear entidad AsIncidencia con relaciones *Obj a IncidenciaQuejaDto correctamente")
    void debeMapearAsIncidenciaAIncidenciaQuejaDto() {
        // Arrange - Tipo Incidencia
        AsTipoInsidencia tipo = new AsTipoInsidencia();
        tipo.setId((short) 1);
        tipo.setTiNombre("QUEJA");
        tipo.setTiDescripcion("Queja ciudadana");

        // Catalogo Privacidad
        AsCatalogo privacidad = new AsCatalogo();
        privacidad.setId((short) 2);
        privacidad.setCaValor("Público");
        privacidad.setCaSeudo("PUB");

        // Catalogo Unidad
        AsCatalogo unidad = new AsCatalogo();
        unidad.setId((short) 9);
        unidad.setCaValor("Atención al Vecino");

        // Persona, Correo, Telefono y Vecino
        AsPersona persona = AsPersona.builder()
                .id(10)
                .peCui("2998123450101")
                .peNombre("Alexander")
                .peApellido("Machic")
                .build();

        AsCorreo correo = AsCorreo.builder()
                .id(1)
                .coCorreo("alexander@ejemplo.com")
                .build();

        AsTelefono telefono = AsTelefono.builder()
                .id(1)
                .teTelefono("55551234")
                .build();

        AsVecino vecino = AsVecino.builder()
                .id(10)
                .vePersona(persona)
                .veCorreo(correo)
                .veTelefono(telefono)
                .build();

        // Domicilio y Dirección
        AsDireccion direccion = AsDireccion.builder()
                .id(5)
                .diDireccion("4ta Calle 2-30 Zona 1")
                .build();

        AsDomicilio domicilio = AsDomicilio.builder()
                .doContador("CNT-00123")
                .doDireccion(direccion)
                .doLatitud("14.6349")
                .doLongitud("-90.5069")
                .build();

        // Incidencia
        Instant ahora = Instant.now();
        AsIncidencia incidencia = new AsIncidencia();
        incidencia.setId(100);
        incidencia.setInTipoIncidenciaObj(tipo);
        incidencia.setInPrivacidadObj(privacidad);
        incidencia.setInUnidadObj(unidad);
        incidencia.setInVecinoObj(vecino);
        incidencia.setInContadorObj(domicilio);
        incidencia.setInDireccion("Frente al parque");
        incidencia.setInEmpleado("CARLOS LOPEZ");
        incidencia.setInFecInsidencia(ahora);
        incidencia.setInComentario("Mala atención en ventanilla");
        incidencia.setInLatitud("14.6349");
        incidencia.setInLongitud("-90.5069");
        incidencia.setInEstado("BORRADOR");
        incidencia.setInFecRegistro(ahora);
        incidencia.setInUsrRegistro("uuid-user-123");

        // Testigos JSON
        var testigosNode = objectMapper.valueToTree(List.of(
                Map.of("nombre", "María Testigo", "telefono", "55554321", "correo", "maria@ejemplo.com")
        ));
        incidencia.setInJsons(testigosNode);

        // Act
        IncidenciaQuejaDto dto = IncidenciaMapper.entityToQuejaDto(incidencia, objectMapper);

        // Assert
        assertNotNull(dto);
        assertEquals(100, dto.getId());
        assertEquals("QUEJA", dto.getTipoIncidencia().nombre());
        assertEquals("Público", dto.getPrivacidad().valor());
        assertEquals("PUB", dto.getPrivacidad().seudo());
        assertEquals("Atención al Vecino", dto.getDependencia().nombre());

        // Vecino
        assertNotNull(dto.getVecino());
        assertEquals(10, dto.getVecino().id());
        assertEquals("2998123450101", dto.getVecino().cui());
        assertEquals("Alexander Machic", dto.getVecino().nombreCompleto());
        assertEquals("alexander@ejemplo.com", dto.getVecino().correo());
        assertEquals("55551234", dto.getVecino().telefono());

        // Domicilio
        assertNotNull(dto.getDomicilio());
        assertEquals("CNT-00123", dto.getDomicilio().contador());
        assertEquals("4ta Calle 2-30 Zona 1", dto.getDomicilio().direccion());

        // Testigos
        assertEquals(1, dto.getTestigos().size());
        assertEquals("María Testigo", dto.getTestigos().get(0).nombre());

        // Datos escalares
        assertEquals("CARLOS LOPEZ", dto.getNombreEmpleado());
        assertEquals("Mala atención en ventanilla", dto.getDescripcion());
        assertEquals("BORRADOR", dto.getEstado());
        assertEquals("uuid-user-123", dto.getUsuarioRegistro());
    }

    @Test
    @DisplayName("Debe manejar entidad nula retornando null")
    void debeRetornarNullSiEntidadEsNull() {
        assertNull(IncidenciaMapper.entityToQuejaDto(null, objectMapper));
    }
}
