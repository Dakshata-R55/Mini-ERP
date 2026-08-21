package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VendorResponse {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private boolean active;
    private Instant createdAt;
}