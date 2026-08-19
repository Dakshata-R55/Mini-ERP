package com.Mini_ERP.dto;

import com.Mini_ERP.model.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private String loginId;
    private String email;
    private UserType userType;
}