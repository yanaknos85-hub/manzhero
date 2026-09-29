package ru.sberbank.ditsib.corpclient.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum DepartmentField implements SortField {
    NAME("departmentName"),
    CODE("code"),
    ID("humanReadableId"),
    LOCATION("location"),
    STATUS("status");
    
    private final String name;
    
}
