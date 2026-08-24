package com.Mini_ERP.service;

import com.Mini_ERP.dto.AccessMatrixResponse;
import com.Mini_ERP.dto.UpdateMyProfileRequest;
import com.Mini_ERP.dto.UserProfileResponse;
import com.Mini_ERP.model.AppUser;
import com.Mini_ERP.repository.UserRepository;
import com.Mini_ERP.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserAvatarStorageService avatarStorageService;
    private final UserAccessMatrixCatalog accessMatrixCatalog;

    public UserProfileResponse getMyProfile() {
        return toResponse(requireCurrentUser());
    }

    @Transactional
    public UserProfileResponse updateMyProfile(UpdateMyProfileRequest request) {
        AppUser user = findManagedCurrentUser();

        if (request.getFullName() != null) {
            String name = request.getFullName().trim();
            if (name.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
            }
            user.setFullName(name);
        }

        user.setAddress(trimOrNull(request.getAddress()));
        user.setMobile(trimOrNull(request.getMobile()));

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse uploadMyAvatar(MultipartFile file) {
        AppUser user = findManagedCurrentUser();
        user.setAvatarUrl(avatarStorageService.store(file));
        return toResponse(userRepository.save(user));
    }

    public UserProfileResponse getUserProfile(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    public AccessMatrixResponse getAccessMatrix(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return accessMatrixCatalog.matrixFor(user.getUserType());
    }

    private AppUser requireCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return details.getUser();
    }

    private AppUser findManagedCurrentUser() {
        AppUser current = requireCurrentUser();
        return userRepository.findById(current.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private UserProfileResponse toResponse(AppUser user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .fullName(user.getFullName())
                .address(user.getAddress())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .userType(user.getUserType())
                .avatarUrl(user.getAvatarUrl())
                .active(user.isActive())
                .build();
    }
}
