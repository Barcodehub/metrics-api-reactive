package com.example.resilient_api.infrastructure.adapters.security;

import com.example.resilient_api.domain.api.JwtPort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.JwtPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtAdapter implements JwtPort {

    private final SecretKey secretKey;

    public JwtAdapter(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<JwtPayload> validateAndExtractPayload(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                Long userId = claims.get("userId", Long.class);
                String email = claims.get("email", String.class);
                Boolean isAdmin = claims.get("isAdmin", Boolean.class);

                return JwtPayload.builder()
                        .userId(userId)
                        .email(email)
                        .isAdmin(isAdmin != null && isAdmin)
                        .build();

            } catch (ExpiredJwtException e) {
                log.error("JWT token has expired", e);
                throw new BusinessException(TechnicalMessage.TOKEN_EXPIRED);
            } catch (MalformedJwtException | UnsupportedJwtException | SignatureException e) {
                log.error("Invalid JWT token", e);
                throw new BusinessException(TechnicalMessage.TOKEN_INVALID);
            } catch (IllegalArgumentException e) {
                log.error("JWT claims string is empty", e);
                throw new BusinessException(TechnicalMessage.TOKEN_INVALID);
            }
        });
    }
}
