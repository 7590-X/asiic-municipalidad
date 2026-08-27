package com.assic.muni.presentation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/public")
public class PublicController {

    @PostMapping("/login")
    public ResponseEntity<Object> login(){
        return null;

        // TODO: Implementar funcionalidades para inicio de sesión con credenciales registradas en Keycloak, utilizando como pasarela spring boot
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(){
        // TODO: investigar sobre cierre de sesion con keycloak, no codificar, presentar un informe técnico de alcance
        return null;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(){
        return null;
        // TODO: investigar sobre refresco de token con keycloak, no codificar, presentar un informe técnico de alcance
    }

    @PostMapping("/registrar-vecino")
    public ResponseEntity<Object> registrarVecino(){
        return null;

        //TODO: Implementar registro de vecino generando cuenta en Keycloak y almacenando data en base de datos asiic
    }
}
