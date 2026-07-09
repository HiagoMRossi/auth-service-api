package com.hiagomrossi.authserviceapi;

import com.hiagomrossi.authserviceapi.config.JwtAuthenticationFilter;
import com.hiagomrossi.authserviceapi.config.RestAuthenticationEntryPoint;
import com.hiagomrossi.authserviceapi.config.SecurityConfig;
import com.hiagomrossi.authserviceapi.controller.UserController;
import com.hiagomrossi.authserviceapi.entity.Role;
import com.hiagomrossi.authserviceapi.repository.UserRepository;
import com.hiagomrossi.authserviceapi.service.CustomUserDetailsService;
import com.hiagomrossi.authserviceapi.service.JwtService;
import com.hiagomrossi.authserviceapi.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class})
class UserControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void meWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Authentication is required. Provide a valid Bearer token."));
    }

    @Test
    void meWithValidTokenShouldReturnAuthenticatedUser() throws Exception {
        String token = "valid-token";
        String email = "hiago@example.com";

        UserDetails userDetails = new User(email, "encoded-password", List.of(() -> "ROLE_USER"));

        com.hiagomrossi.authserviceapi.entity.User user = new com.hiagomrossi.authserviceapi.entity.User();
        user.setId(1L);
        user.setName("Hiago Rossi");
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.of(2026, 7, 9, 10, 0));

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hiago Rossi"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void meWithBlacklistedTokenShouldReturnUnauthorized() throws Exception {
        String token = "blacklisted-token";

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
