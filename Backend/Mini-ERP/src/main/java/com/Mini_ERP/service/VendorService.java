package com.Mini_ERP.service;

import com.Mini_ERP.dto.VendorRequest;
import com.Mini_ERP.dto.VendorResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.model.Vendor;
import com.Mini_ERP.repository.VendorRepository;
import com.Mini_ERP.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final AuditLogService auditLogService;

    public List<VendorResponse> listVendors() {
        return vendorRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public VendorResponse getVendor(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public VendorResponse createVendor(VendorRequest request) {
        String name = request.getName().trim();
        if (vendorRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vendor name already exists");
        }

        Vendor saved = vendorRepository.save(Vendor.builder()
                .name(name)
                .address(trimOrNull(request.getAddress()))
                .phone(trimOrNull(request.getPhone()))
                .email(trimOrNull(request.getEmail()))
                .active(true)
                .build());

        auditLogService.logChange(ErpModule.PURCHASE, saved.getId(), saved.getName(),
                com.Mini_ERP.model.AuditAction.CREATE, "name", null, saved.getName(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public VendorResponse updateVendor(Long id, VendorRequest request) {
        Vendor existing = findActive(id);
        String name = request.getName().trim();

        existing.setName(name);
        existing.setAddress(trimOrNull(request.getAddress()));
        existing.setPhone(trimOrNull(request.getPhone()));
        existing.setEmail(trimOrNull(request.getEmail()));

        Vendor saved = vendorRepository.save(existing);
        return toResponse(saved);
    }

    @Transactional
    public void deleteVendor(Long id) {
        Vendor vendor = findActive(id);
        vendor.setActive(false);
        vendorRepository.save(vendor);

        auditLogService.logChange(ErpModule.PURCHASE, vendor.getId(), vendor.getName(),
                com.Mini_ERP.model.AuditAction.DELETE, "active", true, false, currentUsername());
    }

    Vendor findActive(Long id) {
        return vendorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getLoginId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private VendorResponse toResponse(Vendor vendor) {
        return VendorResponse.builder()
                .id(vendor.getId())
                .name(vendor.getName())
                .address(vendor.getAddress())
                .phone(vendor.getPhone())
                .email(vendor.getEmail())
                .active(vendor.isActive())
                .createdAt(vendor.getCreatedAt())
                .build();
    }
}