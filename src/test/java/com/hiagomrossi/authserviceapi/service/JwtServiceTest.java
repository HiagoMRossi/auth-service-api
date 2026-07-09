package com.hiagomrossi.authserviceapi.service;

import com.hiagomrossi.authserviceapi.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-with-at-least-32-characters-long");
        jwtProperties.setExpiration(3_600_000);
        jwtProperties.setRefreshExpiration(604_800_000);

        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void generateTokenShouldCreateValidAccessToken() {
        String token = jwtService.generateToken("hiago@example.com");

        assertTrue(jwtService.isTokenValid(token));
        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));
        assertEquals("hiago@example.com", jwtService.extractEmail(token));
    }

    @Test
    void generateRefreshTokenShouldCreateValidRefreshToken() {
        String token = jwtService.generateRefreshToken("hiago@example.com");

        assertTrue(jwtService.isTokenValid(token));
        assertTrue(jwtService.isRefreshToken(token));
        assertFalse(jwtService.isAccessToken(token));
        assertEquals("hiago@example.com", jwtService.extractEmail(token));
    }

    @Test
    void invalidTokenShouldReturnFalse() {
        assertFalse(jwtService.isTokenValid("not-a-jwt"));
    }
}
