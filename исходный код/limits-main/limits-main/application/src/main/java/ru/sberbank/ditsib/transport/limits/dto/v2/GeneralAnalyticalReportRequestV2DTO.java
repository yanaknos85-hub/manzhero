package ru.sberbank.ditsib.transport.limits.dto.v2;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.List;
import java.util.UUID;

public record GeneralAnalyticalReportRequestV2DTO(
    List<UUID> organizationId,
    Integer year,
    List<Month> monthList,
    List<TransportTypeEnum> transportType
) {
}
