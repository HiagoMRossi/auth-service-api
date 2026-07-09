package com.hiagomrossi.authserviceapi.service;

import com.hiagomrossi.authserviceapi.dto.LoginRequest;
import com.hiagomrossi.authserviceapi.dto.LoginResponse;
import com.hiagomrossi.authserviceapi.dto.LogoutRequest;
import com.hiagomrossi.authserviceapi.dto.RefreshTokenRequest;
import com.hiagomrossi.authserviceapi.dto.RegisterRequest;
import com.hiagomrossi.authserviceapi.dto.RegisterResponse;
import com.hiagomrossi.authserviceapi.entity.Role;
import com.hiagomrossi.authserviceapi.entity.User;
import com.hiagomrossi.authserviceapi.exception.EmailAlreadyExistsException;
import com.hiagomrossi.authserviceapi.exception.InvalidCredentialsException;
import com.hiagomrossi.authserviceapi.exception.InvalidRefreshTokenException;
import com.hiagomrossi.authserviceapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, tokenBlacklistService);
    }

    @Test
    void registerShouldCreateUserWithUserRole() {
        RegisterRequest request = registerRequest();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.of(2026, 7, 9, 10, 0));
            return user;
        });

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals(1L, response.getId());
        assertEquals("Hiago Rossi", response.getName());
        assertEquals("hiago@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
        assertEquals(Role.USER, userCaptor.getValue().getRole());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
    }

    @Test
    void registerWithDuplicateEmailShouldThrowException() {
        RegisterRequest request = registerRequest();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void loginShouldReturnAccessAndRefreshTokens() {
        LoginRequest request = loginRequest();
        User user = user();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getEmail())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getType());
    }

    @Test
    void loginWithInvalidPasswordShouldThrowException() {
        LoginRequest request = loginRequest();
        User user = user();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refreshShouldReturnNewTokensWhenRefreshTokenIsValid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("refresh-token")).thenReturn(false);
        when(jwtService.extractEmail("refresh-token")).thenReturn("hiago@example.com");
        when(userRepository.existsByEmail("hiago@example.com")).thenReturn(true);
        when(jwtService.generateToken("hiago@example.com")).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("hiago@example.com")).thenReturn("new-refresh-token");

        LoginResponse response = authService.refresh(request);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    void refreshWithBlacklistedTokenShouldThrowException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("refresh-token")).thenReturn(true);

        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(request));
    }

    @Test
    void logoutShouldBlacklistAccessAndRefreshTokens() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        authService.logout("access-token", request);

        verify(tokenBlacklistService).blacklist("access-token");
        verify(tokenBlacklistService).blacklist("refresh-token");
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Hiago Rossi");
        request.setEmail("hiago@example.com");
        request.setPassword("123456");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("hiago@example.com");
        request.setPassword("123456");
        return request;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setName("Hiago Rossi");
        user.setEmail("hiago@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.of(2026, 7, 9, 10, 0));
        return user;
    }
}
