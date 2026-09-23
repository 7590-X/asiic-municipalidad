package com.assic.muni.application.util;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.assic.muni.application.exception.ServiceException;

public final class JwtExtractor {

    public static String extrarJwtSubject() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String subject = jwt.getSubject();
        if (null == subject) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo identificar al usuario");
        }
        return subject;
    }
}
