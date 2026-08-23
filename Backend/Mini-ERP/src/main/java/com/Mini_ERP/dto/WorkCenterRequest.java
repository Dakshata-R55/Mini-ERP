package com.Mini_ERP.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkCenterRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 200)
    private String location;
}
