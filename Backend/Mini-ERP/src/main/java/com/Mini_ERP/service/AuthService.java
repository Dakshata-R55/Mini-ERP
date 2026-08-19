package com.Mini_ERP.service;

import com.Mini_ERP.dto.AuthResponse;
import com.Mini_ERP.dto.LoginRequest;
import com.Mini_ERP.dto.SignupRequest;
import com.Mini_ERP.model.AppUser;
import com.Mini_ERP.model.UserType;
import com.Mini_ERP.repository.UserRepository;
import com.Mini_ERP.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RolePermissionCatalog rolePermissionCatalog;

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
        userRepository.save(user);

        return toResponse(user);
    }

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

        return toResponse(user);
    }

    private AuthResponse toResponse(AppUser user) {
        return AuthResponse.builder()
                .token(jwtTokenProvider.generateToken(user.getLoginId()))
                .tokenType("Bearer")
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .userType(user.getUserType())
                .build();
    }
}