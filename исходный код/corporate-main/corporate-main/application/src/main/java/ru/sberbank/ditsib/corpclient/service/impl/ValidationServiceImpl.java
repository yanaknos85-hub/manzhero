package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.corpclient.service.ValidationService;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of validation service
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {
    private final OrganizationService organizationService;
    private final DepartmentService departmentService;

    @Override
    public void validateDepartmentIds(UUID organizationId, List<UUID> departmentIds) {
        organizationService.validateOrganizationId(organizationId);
        departmentIds.forEach(e -> departmentService.validateDepartmentByIdAndOrgId(e, organizationId));
    }
}
