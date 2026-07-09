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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        RegisterResponse response = new RegisterResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().name());
        response.setCreatedAt(savedUser.getCreatedAt());
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return buildTokenResponse(user.getEmail());
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isTokenValid(refreshToken)
                || !jwtService.isRefreshToken(refreshToken)
                || tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String email = jwtService.extractEmail(refreshToken);
        if (!userRepository.existsByEmail(email)) {
            throw new InvalidRefreshTokenException();
        }

        return buildTokenResponse(email);
    }

    public void logout(String accessToken, LogoutRequest request) {
        tokenBlacklistService.blacklist(accessToken);

        if (request != null) {
            tokenBlacklistService.blacklist(request.getRefreshToken());
        }
    }

    private LoginResponse buildTokenResponse(String email) {
        String accessToken = jwtService.generateToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setType("Bearer");
        return response;
    }
}
