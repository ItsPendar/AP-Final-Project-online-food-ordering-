package org.example.server.Util;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;


public class JWTHandler {
    private static final String SECRET_PASS = // Safer in an encrypted file.
            "@AmirKabirUniversity-APFinalProject";
    private static final SecretKeySpec SECRET_KEY = // same key every runtime.
            new SecretKeySpec(SECRET_PASS.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    public static String generateToken(String subject) {
        if (subject == null || subject.isEmpty()) return null;

        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + 600000000))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims verifyToken(String token) {
        if (token == null || token.isEmpty()) return null;

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)  // For HMAC keys (like HS256)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e ) {
            return null;
        }
    }
}
