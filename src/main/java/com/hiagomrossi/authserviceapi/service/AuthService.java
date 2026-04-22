package com.hiagomrossi.authserviceapi.service;

import com.hiagomrossi.authserviceapi.dto.LoginRequest;
import com.hiagomrossi.authserviceapi.dto.LoginResponse;
import com.hiagomrossi.authserviceapi.dto.RegisterRequest;
import com.hiagomrossi.authserviceapi.dto.RegisterResponse;
import com.hiagomrossi.authserviceapi.entity.User;
import com.hiagomrossi.authserviceapi.exception.EmailAlreadyExistsException;
import com.hiagomrossi.authserviceapi.exception.InvalidCredentialsException;
import com.hiagomrossi.authserviceapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        RegisterResponse response = new RegisterResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setCreatedAt(savedUser.getCreatedAt());
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setType("Bearer");
        return response;
    }
}
