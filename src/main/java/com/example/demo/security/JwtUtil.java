package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static final SecretKey key =
            Keys.secretKeyFor(SignatureAlgorithm.HS256);

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
                .signWith(key)
                .compact();
    }

    // Validate token and return claims
    public static Claims getClaims(String token) {

        if (token == null) {
            throw new RuntimeException("Missing token");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
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