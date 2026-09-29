package ru.sber.transport.tariff_fleet.service.validation.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.tariff_fleet.config.ServicePointsConfig;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.ServicePointsAbsentException;
import ru.sber.transport.tariff_fleet.exception.ServicePointsUniqueException;
import ru.sber.transport.tariff_fleet.exception.ServicePointsValidationException;
import ru.sber.transport.tariff_fleet.exception.TooManyServicePointsException;
import ru.sber.transport.tariff_fleet.service.validation.ServicePointValidationService;

import java.math.BigDecimal;
import java.util.List;

import static ru.sber.transport.tariff_fleet.exception.ServicePointsValidationException.*;

@Service
@RequiredArgsConstructor
public class ServicePointValidationServiceImpl implements ServicePointValidationService {

    public static final int MAX_COORDINATE_SCALE = 6;

    private final ServicePointsConfig servicePointsConfig;


    @Override
    public void validateServicePoint(List<ServicePointDto> servicePoints) {
        if (servicePoints != null && !servicePoints.isEmpty()) {
            validateServicePointsMaxSize(servicePoints);
            validateUniqueServicePoint(servicePoints);
            for (var servicePoint : servicePoints) {
                validateServicePointCoordinates(servicePoint.latitude(), servicePoint.longitude());
            }
        }
    }

    private void validateServicePointsMaxSize(List<ServicePointDto> servicePoints) {
        if (servicePoints.size() > servicePointsConfig.maxRowCount()) {
            throw new TooManyServicePointsException(servicePointsConfig.maxRowCount());
        }
    }

    private void validateUniqueServicePoint(List<ServicePointDto> servicePoints) {
        var uniqueServicePoints = servicePoints.stream()
                .distinct()
                .toList();
        if (uniqueServicePoints.size() != servicePoints.size()) {
            throw new ServicePointsUniqueException();
        }
    }

    private void validateServicePointCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new ServicePointsAbsentException();
        }

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new ServicePointsValidationException(INVALID_LATITUDE_VALUE_MSG);
        }

        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new ServicePointsValidationException(INVALID_LONGITUDE_VALUE_MSG);
        }

        if (latitude.scale() > MAX_COORDINATE_SCALE) {
            throw new ServicePointsValidationException(INVALID_LATITUDE_SCALE_VALUE_MSG, String.valueOf(MAX_COORDINATE_SCALE));
        }

        if (longitude.scale() > MAX_COORDINATE_SCALE) {
            throw new ServicePointsValidationException(INVALID_LONGITUDE_SCALE_VALUE_MSG, String.valueOf(MAX_COORDINATE_SCALE));
        }
    }
}
