package com.Mini_ERP.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VendorRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String address;

    @Size(max = 30)
    private String phone;

    @Email
    @Size(max = 100)
    private String email;
}