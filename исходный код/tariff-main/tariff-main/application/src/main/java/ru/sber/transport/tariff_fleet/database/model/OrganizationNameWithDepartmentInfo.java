package ru.sber.transport.tariff_fleet.database.model;

import java.util.UUID;


public record OrganizationNameWithDepartmentInfo(
        UUID organizationId,
        String organizationName,
        UUID departmentId,
        String departmentName,
        UUID parentId) {
}