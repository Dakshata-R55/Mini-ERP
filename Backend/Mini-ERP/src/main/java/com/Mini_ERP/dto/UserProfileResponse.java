package com.Mini_ERP.dto;

import com.Mini_ERP.model.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String loginId;
    private String fullName;
    private String address;
    private String mobile;
    private String email;
    private UserType userType;
    private String avatarUrl;
    private boolean active;
}
