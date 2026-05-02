package com.manager.minerai.services;

import com.manager.minerai.domain.RefreshToken;
import com.manager.minerai.domain.User;
import com.manager.minerai.dto.request.auth.LoginRequest;
import com.manager.minerai.dto.request.auth.RefreshTokenRequest;
import com.manager.minerai.dto.request.auth.RegisterRequest;
import com.manager.minerai.dto.response.AuthResponse;
import com.manager.minerai.exception.BadRequestException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.RefreshTokenRepository;
import com.manager.minerai.repository.UserRepository;
import com.manager.minerai.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = createAndSaveRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.deleteByUserId(user.getId());

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = createAndSaveRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (!storedToken.isValid()) {
            refreshTokenRepository.delete(storedToken);
            throw new BadRequestException("Refresh token expired or revoked — please login again");
        }

        User user = storedToken.getUser();

        refreshTokenRepository.delete(storedToken);

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());
        String newRefreshToken = createAndSaveRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Transactional
    public void logout(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    private String createAndSaveRefreshToken(User user) {
        String token = jwtUtil.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}