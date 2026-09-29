package ru.sberbank.ditsib.transport.approvals.dto.params;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum EmployeeField implements SortField {
    FULL_NAME("fullName"),
    PERSONNEL_NUMBER("personnelNumber"),
    ID("humanReadableId");
    
    private final String name;
}
