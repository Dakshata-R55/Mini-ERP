package com.Mini_ERP.controller;

import com.Mini_ERP.dto.AvatarUploadResponse;
import com.Mini_ERP.dto.UpdateMyProfileRequest;
import com.Mini_ERP.dto.UserProfileResponse;
import com.Mini_ERP.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public UserProfileResponse getMyProfile() {
        return userProfileService.getMyProfile();
    }

    @PutMapping("/me")
    public UserProfileResponse updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request) {
        return userProfileService.updateMyProfile(request);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AvatarUploadResponse uploadMyAvatar(@RequestParam("file") MultipartFile file) {
        UserProfileResponse profile = userProfileService.uploadMyAvatar(file);
        return new AvatarUploadResponse(profile.getAvatarUrl());
    }
}
