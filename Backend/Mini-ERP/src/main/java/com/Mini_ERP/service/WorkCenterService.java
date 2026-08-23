package com.Mini_ERP.service;

import com.Mini_ERP.dto.WorkCenterRequest;
import com.Mini_ERP.dto.WorkCenterResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.model.WorkCenter;
import com.Mini_ERP.repository.WorkCenterRepository;
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
public class WorkCenterService {

    private final WorkCenterRepository workCenterRepository;
    private final AuditLogService auditLogService;

    public List<WorkCenterResponse> listWorkCenters() {
        return workCenterRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkCenterResponse getWorkCenter(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public WorkCenterResponse createWorkCenter(WorkCenterRequest request) {
        String name = request.getName().trim();
        if (workCenterRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Work center name already exists");
        }

        WorkCenter saved = workCenterRepository.save(WorkCenter.builder()
                .name(name)
                .location(trimOrNull(request.getLocation()))
                .active(true)
                .build());

        auditLogService.logChange(ErpModule.MANUFACTURING, saved.getId(), saved.getName(),
                com.Mini_ERP.model.AuditAction.CREATE, "name", null, saved.getName(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public WorkCenterResponse updateWorkCenter(Long id, WorkCenterRequest request) {
        WorkCenter existing = findActive(id);
        existing.setName(request.getName().trim());
        existing.setLocation(trimOrNull(request.getLocation()));
        return toResponse(workCenterRepository.save(existing));
    }

    @Transactional
    public void deleteWorkCenter(Long id) {
        WorkCenter workCenter = findActive(id);
        workCenter.setActive(false);
        workCenterRepository.save(workCenter);

        auditLogService.logChange(ErpModule.MANUFACTURING, workCenter.getId(), workCenter.getName(),
                com.Mini_ERP.model.AuditAction.DELETE, "active", true, false, currentUsername());
    }

    WorkCenter findActive(Long id) {
        return workCenterRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work center not found"));
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

    private WorkCenterResponse toResponse(WorkCenter workCenter) {
        return WorkCenterResponse.builder()
                .id(workCenter.getId())
                .name(workCenter.getName())
                .location(workCenter.getLocation())
                .active(workCenter.isActive())
                .build();
    }
}
