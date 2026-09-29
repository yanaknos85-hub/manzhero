package ru.sber.transport.tariff_fleet.service.validation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EwbTariffValidationService {

    void validateActiveEwbExistence(List<UUID> departmentIds, LocalDate checkStartDate);


}
