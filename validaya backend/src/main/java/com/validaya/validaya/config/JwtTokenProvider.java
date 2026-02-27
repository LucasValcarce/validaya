package com.validaya.validaya.config;

import com.validaya.validaya.config.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider implements Serializable {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";

    @Value("${security.jwt.token.secret-key}")
    private String secretKeyBase64;

    @Value("${security.jwt.token.expire-ms:28800000}")
    private long expiryMs;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secretKeyBase64);
        } catch (IllegalArgumentException ex) {
            decoded = secretKeyBase64.getBytes(StandardCharsets.UTF_8);
        }
        this.secretKey = Keys.hmacShaKeyFor(decoded);
    }

    public String createToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim(CLAIM_USER_ID, principal.getId())
                .claim(CLAIM_EMAIL, principal.getUsername())
                .claim(CLAIM_ROLE, principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(null))
                .signWith(secretKey)
                .compact();
    }

    public String resolveToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return null;
        if (authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        return authHeader;
    }

    public Optional<Authentication> validateTokenAndGetAuth(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsed.getBody();

            Date exp = claims.getExpiration();
            if (exp == null || exp.before(new Date())) return Optional.empty();

            Long userId = claims.get(CLAIM_USER_ID, Long.class);
            String email = claims.get(CLAIM_EMAIL, String.class);
            String role = claims.get(CLAIM_ROLE, String.class);

            UserPrincipal principal = new UserPrincipal(
                    userId,
                    email,
                    "[PROTECTED]",
                    true,
                    role != null
                            ? java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role))
                            : java.util.List.of()
            );

            return Optional.of(new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities()));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT inválido: {}", e.getMessage());
            return Optional.empty();
        }
    }
}