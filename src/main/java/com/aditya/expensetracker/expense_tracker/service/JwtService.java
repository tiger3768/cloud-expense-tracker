package com.aditya.expensetracker.expense_tracker.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.User;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String secret;
    
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public String generateToken(User user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {

        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public boolean isTokenValid(String token, User user) {

        Claims claims = extractAllClaims(token);

        return claims.getSubject().equals(user.getEmail())
                && claims.getExpiration().after(new Date());
    }
}