package ru.sberbank.ditsib.transport.approvals.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum JournalSortProperty {
    ID("id", "id"),
    DESIRED_DATE("desiredDate", "desired_date"),
    STATUS("status", "status"),
    TRANSPORT_TYPE("transportType", "transport_type"),
    COST("cost", "expected_cost"),
    CREATION_TIME("creationTime", "creation_time");

    private final String sortName;
    private final String columnName;
}
