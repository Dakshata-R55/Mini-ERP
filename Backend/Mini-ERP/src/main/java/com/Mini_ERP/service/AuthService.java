package com.Mini_ERP.service;

import com.Mini_ERP.dto.AuthResponse;
import com.Mini_ERP.dto.LoginRequest;
import com.Mini_ERP.dto.SignupRequest;
import com.Mini_ERP.model.AppUser;
import com.Mini_ERP.model.UserType;
import com.Mini_ERP.repository.UserRepository;
import com.Mini_ERP.security.JwtTokenProvider;
import com.Mini_ERP.security.TokenDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RolePermissionCatalog rolePermissionCatalog;
    private final TokenSessionService tokenSessionService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String loginId = request.getLoginId().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByLoginId(loginId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login id already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        AppUser user = AppUser.builder()
                .loginId(loginId)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(UserType.NONE)
                .active(true)
                .build();

        rolePermissionCatalog.apply(user, UserType.NONE);
        AppUser saved = userRepository.save(user);

        return issueToken(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String loginId = request.getLoginId().trim().toLowerCase();

        AppUser user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        return issueToken(user);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return;
        }

        String token = authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7).trim()
                : authorizationHeader.trim();

        if (!jwtTokenProvider.isValid(token)) {
            return;
        }

        tokenSessionService.revoke(jwtTokenProvider.getJti(token));
    }

    private AuthResponse issueToken(AppUser user) {
        TokenDetails details = jwtTokenProvider.generateTokenDetails(user.getLoginId());
        tokenSessionService.register(user, details.jti(), details.expiresAt());
        return toResponse(user, details.token());
    }

    private AuthResponse toResponse(AppUser user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .userType(user.getUserType())
                .build();
    }
    public AuthResponse currentUser(String loginId) {
    AppUser user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (!user.isActive()) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
    }

    return AuthResponse.builder()
            .loginId(user.getLoginId())
            .email(user.getEmail())
            .userType(user.getUserType())
            .build();
}
}