package com.Mini_ERP.controller;

import com.Mini_ERP.dto.AuthResponse;
import com.Mini_ERP.dto.LoginRequest;
import com.Mini_ERP.dto.SignupRequest;
import com.Mini_ERP.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
    }
    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        return authService.currentUser(authentication.getName());
   }
}