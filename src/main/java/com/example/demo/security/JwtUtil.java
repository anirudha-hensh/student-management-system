package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    // Fixed secret key so JWTs remain valid after server restart
    private static final String SECRET_KEY =
        System.getenv("JWT_SECRET");

private static final SecretKey key;

static {
    if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
        throw new IllegalStateException(
                "JWT_SECRET environment variable is not configured"
        );
    }

    key = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(SECRET_KEY)
    );
}

    private static final long EXPIRATION_TIME =
            1000 * 60 * 60; // 1 hour

    // Generate token
    public static String generateToken(String username, String role) {

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate token and return claims
    public static Claims getClaims(String token) {

        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Missing token");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Get username
    public static String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Get role
    public static String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }
}