package ru.sberbank.ditsib.transport.limits.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApproveLimitRequestMessage(
        
        UUID id,
        
        UUID departmentId,
        
        UUID departmentHeadId,
        
        UUID employeeId,
        
        UUID organizationDepartmentId,
        
        UUID organizationEmployeeId,
        
        UUID limitRequestId,
        
        String humanReadableId,
        
        UUID authorId,
        
        LocalDateTime creationTime,
        
        String transportType,
        
        Integer year,
        
        String period,
        
        String status,
        
        String limitType,
        
        Long sum,
        
        String description,
        
        String declineReason,
        
        Long sumLimit,
        
        String approvalState,
        
        LocalDateTime approvalDate,
        
        boolean deleted
) {}
