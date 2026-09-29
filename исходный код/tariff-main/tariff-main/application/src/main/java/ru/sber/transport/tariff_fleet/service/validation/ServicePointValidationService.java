package ru.sber.transport.tariff_fleet.service.validation;

import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;

import java.util.List;

public interface ServicePointValidationService {
    void validateServicePoint(List<ServicePointDto> servicePoints);
}
