package com.Mini_ERP.config;

import com.Mini_ERP.model.AppUser;
import com.Mini_ERP.model.UserType;
import com.Mini_ERP.repository.UserRepository;
import com.Mini_ERP.service.RolePermissionCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolePermissionCatalog rolePermissionCatalog;

    @Value("${app.system-admin.login-id}")
    private String loginId;

    @Value("${app.system-admin.email}")
    private String email;

    @Value("${app.system-admin.password}")
    private String password;

    @Value("${app.system-admin.full-name}")
    private String fullName;

    @Override
    public void run(String... args) {
        if (userRepository.existsByLoginId(loginId)) {
            return;
        }

        AppUser systemAdmin = AppUser.builder()
                .loginId(loginId)
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .userType(UserType.SYSTEM_ADMIN)
                .active(true)
                .build();

        rolePermissionCatalog.apply(systemAdmin, UserType.SYSTEM_ADMIN);
        userRepository.save(systemAdmin);
    }
}