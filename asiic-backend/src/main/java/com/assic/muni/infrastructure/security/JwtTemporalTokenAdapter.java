package com.assic.muni.infrastructure.security;

import com.assic.muni.application.enums.ETokenAction;
import com.assic.muni.application.port.out.TemporalTokenPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTemporalTokenAdapter implements TemporalTokenPort {

    private final SecretKey secretKey;
    private final long resetPasswordExpiration;
    private final long verifyEmailExpiration;

    private static final String CLAIM_ACTION = "action";
//    private final TokenCompressor tokenCompressor;

    public JwtTemporalTokenAdapter(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration.reset-password}") long resetPasswordExpiration,
            @Value("${app.security.jwt.expiration.verify-email:${app.security.jwt.expiration.verity-email}}") long verifyEmailExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.resetPasswordExpiration = resetPasswordExpiration;
        this.verifyEmailExpiration = verifyEmailExpiration;
    }


    @Override
    public String generateResetPasswordToken(String userId) {
        return buildToken(userId, ETokenAction.RESET_PASSWORD.name(), resetPasswordExpiration);
    }

    @Override
    public String generateVerifyEmailToken(String userId) {
        return buildToken(userId, ETokenAction.VERIFY_EMAIL.name(), verifyEmailExpiration);
    }

    @Override
    public String validateAndExtractUserId(String token, ETokenAction expectedAction) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey)
                    .build().parseSignedClaims(token).getPayload();

            Date expiration = claims.getExpiration();
            Date now = new Date();

            if (expiration.before(now)) {
                throw new JwtException("Token ha expirado, por favor vuelva a generar un nuevo link de confirmación de cuenta");
            }

            String action = claims.get("action", String.class);
            if (!expectedAction.name().equals(action)) {
                throw new JwtException("El token no es válido para esta acción");
            }
            return claims.getSubject();
        } catch (JwtException e) {
            throw e;
        }
    }

    private String buildToken(String userId, String action, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        final String token = Jwts.builder()
                .subject(userId)
                .claim(CLAIM_ACTION, action)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
        return token;
    }
}
