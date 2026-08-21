package com.Mini_ERP.service;

import com.Mini_ERP.dto.CustomerRequest;
import com.Mini_ERP.dto.CustomerResponse;
import com.Mini_ERP.model.Customer;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.repository.CustomerRepository;
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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;
    private final PermissionService permissionService;

    public List<CustomerResponse> listCustomers() {
        return customerRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse getCustomer(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        String name = request.getName().trim();
        if (customerRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer name already exists");
        }

        Customer saved = customerRepository.save(Customer.builder()
                .name(name)
                .address(trimOrNull(request.getAddress()))
                .phone(trimOrNull(request.getPhone()))
                .email(trimOrNull(request.getEmail()))
                .active(true)
                .build());

        auditLogService.logChange(ErpModule.SALES, saved.getId(), saved.getName(),
                com.Mini_ERP.model.AuditAction.CREATE, "name", null, saved.getName(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existing = findActive(id);
        String name = request.getName().trim();

        existing.setName(name);
        existing.setAddress(trimOrNull(request.getAddress()));
        existing.setPhone(trimOrNull(request.getPhone()));
        existing.setEmail(trimOrNull(request.getEmail()));

        Customer saved = customerRepository.save(existing);
        return toResponse(saved);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        requireSalesAdmin();
        Customer customer = findActive(id);
        customer.setActive(false);
        customerRepository.save(customer);

        auditLogService.logChange(ErpModule.SALES, customer.getId(), customer.getName(),
                com.Mini_ERP.model.AuditAction.DELETE, "active", true, false, currentUsername());
    }

    Customer findActive(Long id) {
        return customerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private void requireSalesAdmin() {
        var user = currentUser();
        if (!permissionService.canAdmin(user, ErpModule.SALES)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sales admin access required");
        }
    }

    private com.Mini_ERP.model.AppUser currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    private String currentUsername() {
        return currentUser().getLoginId();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}