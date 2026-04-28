package com.creditbuilder.api.service;

import com.creditbuilder.api.dto.AuthResponse;

import com.creditbuilder.api.dto.LoginRequest;
import com.creditbuilder.api.dto.RegisterRequest;
import com.creditbuilder.api.entity.User;
import com.creditbuilder.api.repository.UserRepository;
import com.creditbuilder.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request){

        //1. Check if email already exists

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already registered");
        }

        // 2. Check if SA ID already exists
        if(request.getSaIdNumber() != null &&
           userRepository.existsBySaIdNumber(request.getSaIdNumber())) {
            throw new RuntimeException("SA ID number already registered");
        }

        // 3. Build the user entity (hashing the passowrd)
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode((request.getPassword())))
                .saIdNumber(request.getSaIdNumber())
                .employmentStatus(request.getEmploymentStatus())
                .build();

        // 4. save user to the database
        User savedUser = userRepository.save(user);

        // 5. Generate JWT token
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getId());
        return AuthResponse.builder()
                .token(token)
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request){

        // 1. Authenticate (throws exception if wrong credentials)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Load user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not Found"));

        String token = jwtService.generateToken(user.getEmail(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .message("Login successful")
                .build();
    }
}
