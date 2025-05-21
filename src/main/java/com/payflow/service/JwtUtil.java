package com.payflow.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    private final String jwtSecret = "vC4gNwGkTPFgN91P7X5RzmMAu4rZ4lM0fOaRpXrYPiM=";
    private final long jwtExpirationMs = 86400000;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().getExpiration();
        return expiration.before(new Date());
    }


        public Claims extractClaims(String token) {
            return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        }
    public String admingenerateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", List.of("ADMIN"))// note: list here
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }


}

