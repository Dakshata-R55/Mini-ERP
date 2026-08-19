package com.Mini_ERP.dto;

import com.Mini_ERP.model.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignUserTypeRequest {

    @NotNull
    private UserType userType;
}