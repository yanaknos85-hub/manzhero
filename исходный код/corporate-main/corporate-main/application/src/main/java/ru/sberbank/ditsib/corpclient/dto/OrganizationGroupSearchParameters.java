package ru.sberbank.ditsib.corpclient.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrganizationGroupSearchParameters implements SortField {

    NAME("name"),

    INTERNAL("internal");

    private final String name;
}
