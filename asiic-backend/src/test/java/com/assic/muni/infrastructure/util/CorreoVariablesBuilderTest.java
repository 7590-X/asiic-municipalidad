package com.assic.muni.infrastructure.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CorreoVariablesBuilder - Pruebas Unitarias")
class CorreoVariablesBuilderTest {

    @Test
    @DisplayName("Debe construir el mapa con valores por defecto y campos asignados")
    void shouldBuildMapWithDefaultsAndAssignedFields() {
        Map<String, Object> variables = CorreoVariablesBuilder.builder()
                .title("Cuenta Creada")
                .messageBody("Bienvenido al sistema")
                .build();

        assertThat(variables)
                .containsEntry(CorreoVariablesBuilder.VAR_TITLE, "Cuenta Creada")
                .containsEntry(CorreoVariablesBuilder.VAR_MESSAGE_BODY, "Bienvenido al sistema")
                .containsEntry(CorreoVariablesBuilder.VAR_HEADER_TITLE, CorreoVariablesBuilder.DEFAULT_HEADER_TITLE)
                .containsEntry(CorreoVariablesBuilder.VAR_HEADER_SUBTITLE, CorreoVariablesBuilder.DEFAULT_HEADER_SUBTITLE)
                .containsEntry(CorreoVariablesBuilder.VAR_FOOTER_TEXT, CorreoVariablesBuilder.DEFAULT_FOOTER_TEXT)
                .doesNotContainKey(CorreoVariablesBuilder.VAR_BUTTON_URL)
                .doesNotContainKey(CorreoVariablesBuilder.VAR_LOGO_URL)
                .doesNotContainKey(CorreoVariablesBuilder.VAR_ITEMS);
    }

    @Test
    @DisplayName("Debe configurar botón con texto y url")
    void shouldConfigureButton() {
        Map<String, Object> variables = CorreoVariablesBuilder.builder()
                .title("Activación")
                .messageBody("Activa tu cuenta")
                .button("Confirmar", "http://localhost/token123")
                .build();

        assertThat(variables)
                .containsEntry(CorreoVariablesBuilder.VAR_BUTTON_TEXT, "Confirmar")
                .containsEntry(CorreoVariablesBuilder.VAR_BUTTON_URL, "http://localhost/token123");
    }

    @Test
    @DisplayName("Debe formatear messageBody con argumentos")
    void shouldFormatMessageBody() {
        Map<String, Object> variables = CorreoVariablesBuilder.builder()
                .messageBody("Hola %s, tu código es %d", "Carlos", 9876)
                .build();

        assertThat(variables)
                .containsEntry(CorreoVariablesBuilder.VAR_MESSAGE_BODY, "Hola Carlos, tu código es 9876");
    }

    @Test
    @DisplayName("Debe gestionar lista de items mediante addItem e items")
    void shouldHandleItems() {
        Map<String, Object> variables = CorreoVariablesBuilder.builder()
                .addItem("Paso 1")
                .addItem("Paso 2")
                .build();

        assertThat(variables).containsKey(CorreoVariablesBuilder.VAR_ITEMS);
        @SuppressWarnings("unchecked")
        List<String> items = (List<String>) variables.get(CorreoVariablesBuilder.VAR_ITEMS);
        assertThat(items).containsExactly("Paso 1", "Paso 2");
    }

    @Test
    @DisplayName("Debe construir Thymeleaf Context directamente")
    void shouldBuildContext() {
        Context context = CorreoVariablesBuilder.builder()
                .title("Notificación")
                .buildContext();

        assertThat(context).isNotNull();
        assertThat(context.getVariable(CorreoVariablesBuilder.VAR_TITLE)).isEqualTo("Notificación");
    }

    @Test
    @DisplayName("Debe procesar con TemplateEngine usando processWith")
    void shouldProcessWithTemplateEngine() {
        TemplateEngine templateEngine = Mockito.mock(TemplateEngine.class);
        when(templateEngine.process(eq("correo-template"), any(Context.class)))
                .thenReturn("<html>Correo Renderizado</html>");

        String html = CorreoVariablesBuilder.builder()
                .title("Prueba")
                .processWith(templateEngine);

        assertThat(html).isEqualTo("<html>Correo Renderizado</html>");
        verify(templateEngine).process(eq("correo-template"), any(Context.class));
    }
}
