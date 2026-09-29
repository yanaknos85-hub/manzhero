package ru.sberbank.ditsib.corpclient.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum OrganizationField implements SortField {
    OFFICIAL_NAME("officialName"),
    ADDRESS("address"),
    MSRN("msrn"),
    TID("tid"),
    ORGANIZATION_CODE("organizationCode"),
    GROUP_ID("groupId");
    
    private final String name;
}
