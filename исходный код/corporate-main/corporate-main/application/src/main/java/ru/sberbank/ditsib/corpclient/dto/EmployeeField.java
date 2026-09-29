package ru.sberbank.ditsib.corpclient.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum EmployeeField implements SortField {
    FULL_NAME("fullName"),
    PERSONNEL_NUMBER("personnelNumber"),
    ID("humanReadableId"),
    STATUS("status"),
    MOBILE("mobilePhone"),
    EMAIL("email");
    
    private final String name;
}
