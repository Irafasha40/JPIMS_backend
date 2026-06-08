package com.whizupp.jpims.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";
    public static final String TOKEN_TYPE_PASSWORD_CHANGE_REQUIRED = "PASSWORD_CHANGE_REQUIRED";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /** Short-lived token for mandatory first-login password change (ms). */
    @Value("${jwt.first-login-token-expiry:3600000}")
    private long firstLoginTokenExpiry;

    public String generateToken(UserDetails user) {
        return buildToken(user, accessTokenExpiry, TOKEN_TYPE_ACCESS);
    }

    public String generateRefreshToken(UserDetails user) {
        return buildToken(user, refreshTokenExpiry, TOKEN_TYPE_REFRESH);
    }

    public String generatePasswordChangeRequiredToken(UserDetails user) {
        return buildToken(user, firstLoginTokenExpiry, TOKEN_TYPE_PASSWORD_CHANGE_REQUIRED);
    }

    public boolean validateToken(String token, UserDetails user) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject().equals(user.getUsername()) && claims.getExpiration().after(new Date());
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("tokenType", String.class);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }

    private String buildToken(UserDetails user, long expiryMs, String tokenType) {
        Date now = new Date();
        Date tokenExpiry = new Date(now.getTime() + expiryMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getUsername());
        claims.put("role", extractRoleFromAuthorities(user));
        if (user instanceof AppUserDetails appUserDetails) {
            claims.put("userId", appUserDetails.getId().toString());
        }
        claims.put("tokenType", tokenType);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(tokenExpiry)
                .signWith(getKey())
                .compact();
    }

    private String extractRoleFromAuthorities(UserDetails user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(value -> value.replace("ROLE_", ""))
                .orElse("UNKNOWN");
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
