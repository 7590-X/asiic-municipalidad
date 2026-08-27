package com.assic.muni.application.config;


import com.assic.muni.application.security.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableFeignClients
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        // Permitir swagger sin seguridad pero solo en localhost
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .access((authentication, context) -> {
                            final String IP = context.getRequest().getRemoteAddr();
                            boolean isLocalHost = "127.0.0.1".equals(IP)
                                    || "0:0:0:0:0:0:0:1".equals(IP)
                                    || "::1".equals(IP);

                            return new AuthorizationDecision(isLocalHost);
                        })
                        // Permitir endpoints sin autenticación ni autorización
                        .requestMatchers("/api/v1/asiic/public/**").permitAll()
                        // Todas las demás request requieren token válido
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );
        return http.build();
    }

}
