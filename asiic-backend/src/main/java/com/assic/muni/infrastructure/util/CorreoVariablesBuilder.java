package com.assic.muni.infrastructure.util;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 * Clase utilitaria basada en el patrón Builder para estandarizar
 * y construir el mapa de variables requeridas por la plantilla {@code correo-template.html}.
 */
public class CorreoVariablesBuilder {

    // Constantes de las claves de variables definidas en correo-template.html
    public static final String VAR_TITLE = "title";
    public static final String VAR_HEADER_TITLE = "headerTitle";
    public static final String VAR_HEADER_SUBTITLE = "headerSubtitle";
    public static final String VAR_LOGO_URL = "logoUrl";
    public static final String VAR_MESSAGE_BODY = "messageBody";
    public static final String VAR_ITEMS = "items";
    public static final String VAR_BUTTON_TEXT = "buttonText";
    public static final String VAR_BUTTON_URL = "buttonUrl";
    public static final String VAR_FOOTER_TEXT = "footerText";

    // Constantes por defecto
    public static final String DEFAULT_TEMPLATE_NAME = "correo-template";
    public static final String DEFAULT_HEADER_TITLE = "ASIIC Municipalidades";
    public static final String DEFAULT_HEADER_SUBTITLE = "SISTEMAS DE INFORMACIÓN PARA TODO EL MUNDO";
    public static final String DEFAULT_FOOTER_TEXT = "Todos los derechos reservados a Municipalidades de Guatemala";
    public static final String DEFAULT_BUTTON_TEXT = "Continuar";

    private String title;
    private String headerTitle = DEFAULT_HEADER_TITLE;
    private String headerSubtitle = DEFAULT_HEADER_SUBTITLE;
    private String logoUrl;
    private String messageBody;
    private List<String> items = new ArrayList<>();
    private String buttonText = DEFAULT_BUTTON_TEXT;
    private String buttonUrl;
    private String footerText = DEFAULT_FOOTER_TEXT;

    private CorreoVariablesBuilder() {
    }

    /**
     * Punto de entrada estático para instanciar el builder.
     */
    public static CorreoVariablesBuilder builder() {
        return new CorreoVariablesBuilder();
    }

    /**
     * Título principal del correo (usado en la etiqueta {@code <title>} y encabezado {@code <h1>}).
     */
    public CorreoVariablesBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Título institucional superior (ej. "ASIIC").
     */
    public CorreoVariablesBuilder headerTitle(String headerTitle) {
        this.headerTitle = headerTitle;
        return this;
    }

    /**
     * Subtítulo institucional superior (ej. "MUNICIPALIDADES DE GUATEMALA").
     */
    public CorreoVariablesBuilder headerSubtitle(String headerSubtitle) {
        this.headerSubtitle = headerSubtitle;
        return this;
    }

    /**
     * URL de la imagen del logotipo institucional.
     */
    public CorreoVariablesBuilder logoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
        return this;
    }

    /**
     * Cuerpo principal del mensaje. Admite etiquetas HTML gracias a {@code th:utext}.
     */
    public CorreoVariablesBuilder messageBody(String messageBody) {
        this.messageBody = messageBody;
        return this;
    }

    /**
     * Permite construir el cuerpo principal con formato de forma funcional (tipo {@link String#format}).
     */
    public CorreoVariablesBuilder messageBody(String format, Object... args) {
        if (format != null) {
            this.messageBody = String.format(format, args);
        }
        return this;
    }

    /**
     * Agrega un item individual a la lista numerada dinámica.
     */
    public CorreoVariablesBuilder addItem(String item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        if (item != null) {
            this.items.add(item);
        }
        return this;
    }

    /**
     * Asigna una lista de items para la sección numerada dinámica.
     */
    public CorreoVariablesBuilder items(List<String> items) {
        if (items != null) {
            this.items = new ArrayList<>(items);
        }
        return this;
    }

    /**
     * Asigna múltiples items mediante varargs.
     */
    public CorreoVariablesBuilder items(String... items) {
        if (items != null) {
            this.items = new ArrayList<>(Arrays.asList(items));
        }
        return this;
    }

    /**
     * Configura el botón de acción principal indicando texto y URL de redirección.
     */
    public CorreoVariablesBuilder button(String text, String url) {
        this.buttonText = text;
        this.buttonUrl = url;
        return this;
    }

    /**
     * Texto visible dentro del botón de acción.
     */
    public CorreoVariablesBuilder buttonText(String buttonText) {
        this.buttonText = buttonText;
        return this;
    }

    /**
     * Enlace de destino del botón de acción.
     */
    public CorreoVariablesBuilder buttonUrl(String buttonUrl) {
        this.buttonUrl = buttonUrl;
        return this;
    }

    /**
     * Texto para el pie de página institucional. Por defecto "Municipalidades de Guatemala".
     */
    public CorreoVariablesBuilder footerText(String footerText) {
        this.footerText = footerText;
        return this;
    }

    /**
     * Remueve encabezados institucionales por defecto si se desea una cabecera vacía.
     */
    public CorreoVariablesBuilder withoutInstitutionalHeaders() {
        this.headerTitle = null;
        this.headerSubtitle = null;
        return this;
    }

    /**
     * Construye y retorna el {@link Map} con las variables estandarizadas
     * listas para ser consumidas por Thymeleaf.
     */
    public Map<String, Object> build() {
        Map<String, Object> variables = new HashMap<>();

        if (this.title != null) {
            variables.put(VAR_TITLE, this.title);
        }
        if (this.headerTitle != null) {
            variables.put(VAR_HEADER_TITLE, this.headerTitle);
        }
        if (this.headerSubtitle != null) {
            variables.put(VAR_HEADER_SUBTITLE, this.headerSubtitle);
        }
        if (this.logoUrl != null) {
            variables.put(VAR_LOGO_URL, this.logoUrl);
        }
        if (this.messageBody != null) {
            variables.put(VAR_MESSAGE_BODY, this.messageBody);
        }
        if (this.items != null && !this.items.isEmpty()) {
            variables.put(VAR_ITEMS, Collections.unmodifiableList(new ArrayList<>(this.items)));
        }
        if (this.buttonUrl != null) {
            variables.put(VAR_BUTTON_URL, this.buttonUrl);
            variables.put(VAR_BUTTON_TEXT, this.buttonText != null ? this.buttonText : DEFAULT_BUTTON_TEXT);
        }
        if (this.footerText != null) {
            variables.put(VAR_FOOTER_TEXT, this.footerText);
        }

        return variables;
    }

    /**
     * Utilidad funcional que construye directamente la instancia de {@link Context}
     * de Thymeleaf cargada con el mapa de variables.
     */
    public Context buildContext() {
        Context context = new Context();
        context.setVariables(build());
        return context;
    }

    /**
     * Utilidad funcional para renderizar directamente el HTML usando el {@link TemplateEngine}
     * y la plantilla por defecto {@code correo-template}.
     */
    public String processWith(TemplateEngine templateEngine) {
        return processWith(templateEngine, DEFAULT_TEMPLATE_NAME);
    }

    /**
     * Utilidad funcional para renderizar directamente el HTML usando un nombre de plantilla personalizado.
     */
    public String processWith(TemplateEngine templateEngine, String templateName) {
        Objects.requireNonNull(templateEngine, "El templateEngine no puede ser nulo");
        String template = (templateName != null && !templateName.isBlank()) ? templateName : DEFAULT_TEMPLATE_NAME;
        return templateEngine.process(template, buildContext());
    }
}
