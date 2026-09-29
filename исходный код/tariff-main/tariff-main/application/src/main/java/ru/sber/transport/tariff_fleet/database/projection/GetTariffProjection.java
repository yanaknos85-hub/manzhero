package ru.sber.transport.tariff_fleet.database.projection;

import java.util.UUID;

public interface GetTariffProjection {
    UUID getId();
    String getHumanReadableId();
    String getOrganizationName();
    String getContractorName();
    String getDepartmentName();
    String getContractNumber();
    Boolean getActive();
}
