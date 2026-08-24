package com.Mini_ERP.controller;

import com.Mini_ERP.dto.AccessMatrixResponse;
import com.Mini_ERP.dto.AssignUserTypeRequest;
import com.Mini_ERP.dto.UserProfileResponse;
import com.Mini_ERP.dto.UserSummaryResponse;
import com.Mini_ERP.service.UserAdminService;
import com.Mini_ERP.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAdminService userAdminService;
    private final UserProfileService userProfileService;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<UserSummaryResponse> listUsers() {
        return userAdminService.listUsers();
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public UserProfileResponse getUserProfile(@PathVariable Long id) {
        return userProfileService.getUserProfile(id);
    }

    @GetMapping("/{id}/access-matrix")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public AccessMatrixResponse getAccessMatrix(@PathVariable Long id) {
        return userProfileService.getAccessMatrix(id);
    }

    @PatchMapping("/{id}/user-type")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public UserSummaryResponse assignUserType(
            @PathVariable Long id,
            @Valid @RequestBody AssignUserTypeRequest request) {
        return userAdminService.assignUserType(id, request.getUserType());
    }
}
