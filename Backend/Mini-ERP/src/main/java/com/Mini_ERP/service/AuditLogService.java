package com.Mini_ERP.service;

import com.Mini_ERP.dto.AuditLogResponse;
import com.Mini_ERP.model.AuditAction;
import com.Mini_ERP.model.AuditLog;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logChange(
            ErpModule module,
            Long entityId,
            String entityReference,
            AuditAction action,
            String fieldName,
            Object oldValue,
            Object newValue,
            String changedBy) {

        if (Objects.equals(String.valueOf(oldValue), String.valueOf(newValue))) {
            return;
        }

        AuditLog log = AuditLog.builder()
                .module(module)
                .entityId(entityId)
                .entityReference(entityReference)
                .action(action)
                .fieldName(fieldName)
                .oldValue(oldValue == null ? null : String.valueOf(oldValue))
                .newValue(newValue == null ? null : String.valueOf(newValue))
                .changedBy(changedBy)
                .build();

        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getLogsForEntity(ErpModule module, Long entityId) {
        return auditLogRepository.findByModuleAndEntityIdOrderByChangedAtDesc(module, entityId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getLogsByModule(ErpModule module) {
        return auditLogRepository.findByModuleOrderByChangedAtDesc(module)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .module(log.getModule())
                .entityId(log.getEntityId())
                .entityReference(log.getEntityReference())
                .action(log.getAction())
                .fieldName(log.getFieldName())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .changedBy(log.getChangedBy())
                .changedAt(log.getChangedAt())
                .build();
    }
}