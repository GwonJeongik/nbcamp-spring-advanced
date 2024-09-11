package org.example.expert.domain.manager.controller;

import io.jsonwebtoken.Claims;
import org.example.expert.config.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class JwtAuthService {

    private final JwtUtil jwtUtil;

    public JwtAuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public long getUserIdByToken(String bearerToken) {

        String token;

        if (bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        } else {
            token = bearerToken;
        }

        Claims claims = jwtUtil.extractClaims(token);
        return Long.parseLong(claims.getSubject());
    }
}