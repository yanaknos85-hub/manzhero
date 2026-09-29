package ru.sberbank.ditsib.corpclient.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ExecutorGroupField implements SortField {
    ID("humanReadableId"),
    EXECUTOR_GROUP_NAME("executorGroupName"),
    EXECUTOR_ORGANIZATION("executorOrganization"),
    EXECUTOR_FIO("executorFIO"),
    EXECUTOR_PERSONAL_NUMBER("executorPersonnelNumber"),
    CUSTOMER_ORGANIZATION("customerOrganizations"),
    CUSTOMER_DEPARTMENT("customerDepartments"),
    SERVICE_TYPE("serviceType"),
    CUSTOMER_GEO_ZONE("customerGeoZones");

    private final String name;
}
