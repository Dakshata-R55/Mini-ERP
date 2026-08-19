package com.Mini_ERP.dto;

import com.Mini_ERP.model.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {

    private Long id;
    private String loginId;
    private String email;
    private String fullName;
    private UserType userType;
    private boolean active;
}