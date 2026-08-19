package com.Mini_ERP.service;

import com.Mini_ERP.dto.UserSummaryResponse;
import com.Mini_ERP.model.AppUser;
import com.Mini_ERP.model.UserType;
import com.Mini_ERP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final RolePermissionCatalog rolePermissionCatalog;

    public List<UserSummaryResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public UserSummaryResponse assignUserType(Long userId, UserType userType) {
        if (userType == UserType.SYSTEM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign SYSTEM_ADMIN");
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isSystemAdmin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change system admin");
        }

        rolePermissionCatalog.apply(user, userType);
        userRepository.save(user);

        return toSummary(user);
    }

    private UserSummaryResponse toSummary(AppUser user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userType(user.getUserType())
                .active(user.isActive())
                .build();
    }
}