package com.Mini_ERP.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMyProfileRequest {

    @Size(max = 200)
    private String fullName;

    @Size(max = 500)
    private String address;

    @Size(max = 30)
    private String mobile;
}
